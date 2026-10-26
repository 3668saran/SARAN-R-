from flask import Blueprint, request, jsonify
from backend.auth_service import require_auth, require_role
from backend.validators import validate_reg_number, sanitize_string
from backend.ai_service import GemmaAIService
import time

trustcheck_bp = Blueprint("trustcheck", __name__, url_prefix="/api/trustcheck")
ai_service = GemmaAIService()

SUBMISSIONS = [
    {
        "id": "sub-8921",
        "studentId": "user-std-101",
        "studentName": "Alex Morgan",
        "collegeRegNo": "REG-2024-CS-8921",
        "documentType": "Semester Grade Marksheet",
        "documentName": "marksheet_sem4.pdf",
        "status": "verified",
        "aiNotes": "Official seal and registrar signature pattern detected.",
        "remarks": "Verified against institutional registry database.",
        "reviewedBy": "Dr. Vance",
        "reviewedAt": "Oct 25, 2026"
    }
]

AUDIT_LOGS = [
    {
        "id": "aud-1",
        "action": "DOCUMENT_VERIFIED",
        "operator": "Dr. Vance (staff)",
        "target": "Submission #sub-8921",
        "timestamp": "2026-10-25 14:22:10",
        "details": "Verified academic transcript for Alex Morgan"
    }
]

@trustcheck_bp.route("/submit", methods=["POST"])
@require_auth
def submit_verification():
    data = request.get_json() or {}
    raw_reg = data.get("collegeRegNumber", "")
    valid, reg_no = validate_reg_number(raw_reg)
    if not valid:
        return jsonify({"success": False, "error": reg_no}), 400
        
    doc_type = sanitize_string(data.get("documentType", "Semester Grade Marksheet"), 100)
    doc_name = sanitize_string(data.get("documentName", "marksheet.pdf"), 100)
    
    # Run Gemma AI Pre-Check
    ai_precheck = ai_service.precheck_document(doc_type, reg_no)
    
    user = request.user
    sub = {
        "id": f"sub-{int(time.time())}",
        "studentId": user.get("uid"),
        "studentName": user.get("name", "Student"),
        "collegeRegNo": reg_no,
        "documentType": doc_type,
        "documentName": doc_name,
        "status": "pending",
        "aiNotes": str(ai_precheck.get("extracted", {})),
        "remarks": "In review queue by institutional staff.",
        "reviewedBy": "",
        "reviewedAt": ""
    }
    SUBMISSIONS.insert(0, sub)
    
    AUDIT_LOGS.insert(0, {
        "id": f"aud-{int(time.time())}",
        "action": "DOCUMENT_SUBMITTED",
        "operator": f"{user.get('name')} ({user.get('role')})",
        "target": sub["id"],
        "timestamp": time.strftime("%Y-%m-%d %H:%M:%S"),
        "details": f"Submitted {doc_type} with reg # {reg_no}"
    })
    
    return jsonify({
        "success": True,
        "submission": sub,
        "aiPrecheck": ai_precheck
    }), 201

@trustcheck_bp.route("/my-submission", methods=["GET"])
@require_auth
def get_my_submission():
    uid = request.user.get("uid")
    sub = next((s for s in SUBMISSIONS if s["studentId"] == uid), None)
    return jsonify({"success": True, "submission": sub})

@trustcheck_bp.route("/staff/queue", methods=["GET"])
@require_role(["staff", "admin"])
def get_staff_queue():
    return jsonify({"success": True, "submissions": SUBMISSIONS})

@trustcheck_bp.route("/staff/review", methods=["POST"])
@require_role(["staff", "admin"])
def review_submission():
    data = request.get_json() or {}
    sub_id = data.get("submissionId")
    approved = data.get("approved", False)
    remarks = sanitize_string(data.get("remarks", ""), 500)
    
    if not sub_id:
        return jsonify({"success": False, "error": "Submission ID required"}), 400
    if not approved and not remarks:
        return jsonify({"success": False, "error": "Reason is mandatory when requesting corrections or rejecting"}), 400
        
    sub = next((s for s in SUBMISSIONS if s["id"] == sub_id), None)
    if not sub:
        return jsonify({"success": False, "error": "Submission not found"}), 404
        
    user = request.user
    sub["status"] = "verified" if approved else "rejected"
    sub["remarks"] = remarks or ("Approved and verified by registry." if approved else "Correction requested.")
    sub["reviewedBy"] = user.get("name", "Staff Member")
    sub["reviewedAt"] = time.strftime("%Y-%m-%d %H:%M:%S")
    
    AUDIT_LOGS.insert(0, {
        "id": f"aud-{int(time.time())}",
        "action": "SUBMISSION_APPROVED" if approved else "SUBMISSION_REJECTED",
        "operator": f"{user.get('name')} ({user.get('role')})",
        "target": sub_id,
        "timestamp": time.strftime("%Y-%m-%d %H:%M:%S"),
        "details": f"Decision: {'Verified' if approved else 'Rejected'}. Remarks: {sub['remarks']}"
    })
    
    return jsonify({"success": True, "submission": sub})

@trustcheck_bp.route("/audit", methods=["GET"])
@require_role(["staff", "admin"])
def get_audit_trail():
    return jsonify({"success": True, "logs": AUDIT_LOGS})
