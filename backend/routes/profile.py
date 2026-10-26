from flask import Blueprint, request, jsonify
from backend.auth_service import require_auth
from backend.validators import sanitize_string
import time

profile_bp = Blueprint("profile", __name__, url_prefix="/api")

PROFILES = {
    "user-std-101": {
        "name": "Alex Morgan",
        "college": "National Institute of Science & Technology",
        "course": "Computer Science"
    }
}

ACADEMIC_RECORDS = [
    {
        "id": "rec-1",
        "ownerUid": "user-std-101",
        "subject": "Operating Systems",
        "marks": 92,
        "maximumMarks": 100,
        "verificationStatus": "verified"
    }
]

# PUT /api/profile
@profile_bp.route("/profile", methods=["PUT"])
@require_auth
def update_profile():
    data = request.get_json() or {}
    name = data.get("name", "").strip()
    college = data.get("college", "").strip()
    course = data.get("course", "").strip()

    if not name or not college or not course:
        return jsonify({"error": "Enter valid profile details."}), 400

    user_id = request.user["uid"]
    PROFILES[user_id] = {
        "name": sanitize_string(name, 80),
        "college": sanitize_string(college, 120),
        "course": sanitize_string(course, 100),
        "updatedAt": time.strftime("%Y-%m-%d %H:%M:%S")
    }

    return jsonify({"message": "Profile saved successfully."})

# GET /api/profile
@profile_bp.route("/profile", methods=["GET"])
@require_auth
def get_profile():
    user_id = request.user["uid"]
    profile = PROFILES.get(user_id)
    if not profile:
        return jsonify({"error": "Profile not found."}), 404
    return jsonify(profile)

# POST /api/marks
@profile_bp.route("/marks", methods=["POST"])
@require_auth
def submit_marks():
    data = request.get_json() or {}
    subject = data.get("subject", "").strip()
    marks = data.get("marks")
    maximum_marks = data.get("maximumMarks")

    if not subject or not isinstance(marks, int) or not isinstance(maximum_marks, int):
        return jsonify({"error": "Invalid subject or marks."}), 400
    if maximum_marks < 1 or maximum_marks > 1000 or marks < 0 or marks > maximum_marks:
        return jsonify({"error": "Invalid subject or marks."}), 400

    record = {
        "id": f"rec-{int(time.time())}",
        "ownerUid": request.user["uid"],
        "subject": sanitize_string(subject, 100),
        "marks": marks,
        "maximumMarks": maximum_marks,
        "verificationStatus": "pending"
    }
    ACADEMIC_RECORDS.append(record)

    return jsonify({
        "message": "Marks submitted for verification.",
        "recordId": record["id"],
        "verificationStatus": "pending"
    }), 201

# GET /api/marks
@profile_bp.route("/marks", methods=["GET"])
@require_auth
def get_marks():
    user_id = request.user["uid"]
    user_records = [r for r in ACADEMIC_RECORDS if r.get("ownerUid") == user_id]
    return jsonify(user_records)
