from flask import Blueprint, request, jsonify
from backend.auth_service import require_auth
from backend.validators import sanitize_string
import time

skills_bp = Blueprint("skills", __name__, url_prefix="/api/skills")

SKILL_LISTINGS = [
    {
        "id": "skill-1",
        "userId": "u-201",
        "userName": "Elena Rostova",
        "teachSkills": ["Python", "Pandas", "FastAPI"],
        "learnSkills": ["React", "CSS"],
        "rating": 4.9,
        "completedSwaps": 12
    },
    {
        "id": "skill-2",
        "userId": "u-202",
        "userName": "Karan Patel",
        "teachSkills": ["Linear Algebra", "Calculus II"],
        "learnSkills": ["Machine Learning", "PyTorch"],
        "rating": 4.8,
        "completedSwaps": 7
    }
]

EXCHANGE_REQUESTS = [
    {
        "id": "req-1",
        "fromUserId": "u-201",
        "fromUserName": "Elena Rostova",
        "toUserId": "user-std-101",
        "teachOffered": "Python Data Analysis",
        "learnRequested": "Kotlin",
        "message": "Interested in trading study hours on programming.",
        "status": "pending",
        "date": "Today"
    }
]

@skills_bp.route("/listings", methods=["GET"])
def get_listings():
    query = request.args.get("q", "").lower()
    results = SKILL_LISTINGS
    if query:
        results = [
            s for s in results 
            if query in s["userName"].lower() or 
               any(query in t.lower() for t in s["teachSkills"]) or
               any(query in l.lower() for l in s["learnSkills"])
        ]
    return jsonify({"success": True, "listings": results})

@skills_bp.route("/requests", methods=["POST"])
@require_auth
def create_request():
    data = request.get_json() or {}
    user = request.user
    to_user_name = sanitize_string(data.get("toUserName", ""), 100)
    teach_offered = sanitize_string(data.get("teachOffered", ""), 100)
    learn_requested = sanitize_string(data.get("learnRequested", ""), 100)
    message = sanitize_string(data.get("message", ""), 500)
    
    if not to_user_name or not teach_offered or not learn_requested:
        return jsonify({"success": False, "error": "Missing required fields"}), 400
        
    req_item = {
        "id": f"req-{int(time.time())}",
        "fromUserId": user.get("uid"),
        "fromUserName": user.get("name", "Student"),
        "toUserId": data.get("toUserId", "peer"),
        "toUserName": to_user_name,
        "teachOffered": teach_offered,
        "learnRequested": learn_requested,
        "message": message,
        "status": "pending",
        "date": "Today"
    }
    EXCHANGE_REQUESTS.insert(0, req_item)
    return jsonify({"success": True, "request": req_item}), 201

@skills_bp.route("/my-requests", methods=["GET"])
@require_auth
def get_my_requests():
    user = request.user
    uid = user.get("uid")
    name = user.get("name")
    
    my_requests = [
        r for r in EXCHANGE_REQUESTS 
        if r.get("fromUserId") == uid or r.get("toUserId") == uid or r.get("fromUserName") == name or r.get("toUserName") == name
    ]
    return jsonify({"success": True, "requests": my_requests})

@skills_bp.route("/requests/<req_id>/status", methods=["PUT"])
@require_auth
def update_request_status(req_id):
    data = request.get_json() or {}
    new_status = data.get("status")
    if new_status not in ["accepted", "rejected", "completed"]:
        return jsonify({"success": False, "error": "Invalid status value"}), 400
        
    req_item = next((r for r in EXCHANGE_REQUESTS if r["id"] == req_id), None)
    if not req_item:
        return jsonify({"success": False, "error": "Request not found"}), 404
        
    req_item["status"] = new_status
    return jsonify({"success": True, "request": req_item})
