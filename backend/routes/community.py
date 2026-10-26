from flask import Blueprint, request, jsonify
from backend.auth_service import require_auth
from backend.validators import validate_post_data, sanitize_string
import time

community_bp = Blueprint("community", __name__, url_prefix="/api/community")

# In-memory store for development & tests
POSTS = [
    {
        "id": "post-1",
        "authorId": "user-std-102",
        "authorName": "Sophia Chen",
        "authorRole": "student",
        "authorVerified": True,
        "title": "Best resources for mastering Operating System concurrency?",
        "content": "Looking for clear diagrams on Semaphores, Mutexes, and avoiding Deadlocks.",
        "subject": "Computer Science",
        "upvotes": 24,
        "upvotedBy": [],
        "commentsCount": 8,
        "timestamp": "2 hours ago",
        "isReported": False
    },
    {
        "id": "post-2",
        "authorId": "user-staff-301",
        "authorName": "Prof. Daniel Vance",
        "authorRole": "staff",
        "authorVerified": True,
        "title": "Academic Guidelines: Citing AI tools in Research Papers",
        "content": "A reminder to document prompts in appendix when using LLMs for research synthesis.",
        "subject": "Academic Policy",
        "upvotes": 56,
        "upvotedBy": [],
        "commentsCount": 14,
        "timestamp": "Yesterday",
        "isReported": False
    }
]

@community_bp.route("/posts", methods=["GET"])
def get_posts():
    subject = request.args.get("subject")
    search = request.args.get("search", "").lower()
    
    results = POSTS
    if subject and subject.lower() != "all":
        results = [p for p in results if p["subject"].lower() == subject.lower()]
    if search:
        results = [p for p in results if search in p["title"].lower() or search in p["content"].lower()]
        
    return jsonify({"success": True, "posts": results})

@community_bp.route("/posts", methods=["POST"])
@require_auth
def create_post():
    data = request.get_json() or {}
    valid, payload = validate_post_data(data)
    if not valid:
        return jsonify({"success": False, "error": payload}), 400
        
    user = request.user
    new_post = {
        "id": f"post-{int(time.time())}",
        "authorId": user.get("uid"),
        "authorName": user.get("name", "Student"),
        "authorRole": user.get("role", "student"),
        "authorVerified": user.get("verified", False),
        "title": payload["title"],
        "content": payload["content"],
        "subject": payload["subject"],
        "upvotes": 0,
        "upvotedBy": [],
        "commentsCount": 0,
        "timestamp": "Just now",
        "isReported": False
    }
    POSTS.insert(0, new_post)
    return jsonify({"success": True, "post": new_post}), 201

@community_bp.route("/posts/<post_id>/upvote", methods=["POST"])
@require_auth
def upvote_post(post_id):
    user_id = request.user.get("uid")
    post = next((p for p in POSTS if p["id"] == post_id), None)
    if not post:
        return jsonify({"success": False, "error": "Post not found"}), 404
        
    if user_id in post["upvotedBy"]:
        post["upvotedBy"].remove(user_id)
        post["upvotes"] = max(0, post["upvotes"] - 1)
        upvoted = False
    else:
        post["upvotedBy"].append(user_id)
        post["upvotes"] += 1
        upvoted = True
        
    return jsonify({"success": True, "upvotes": post["upvotes"], "hasUpvoted": upvoted})

@community_bp.route("/posts/<post_id>/report", methods=["POST"])
@require_auth
def report_post(post_id):
    post = next((p for p in POSTS if p["id"] == post_id), None)
    if not post:
        return jsonify({"success": False, "error": "Post not found"}), 404
    post["isReported"] = True
    return jsonify({"success": True, "message": "Post reported to moderation team"})

@community_bp.route("/posts/<post_id>", methods=["DELETE"])
@require_auth
def delete_post(post_id):
    user = request.user
    post = next((p for p in POSTS if p["id"] == post_id), None)
    if not post:
        return jsonify({"success": False, "error": "Post not found"}), 404
        
    # Check ownership or admin
    if post["authorId"] != user.get("uid") and user.get("role") != "admin":
        return jsonify({"success": False, "error": "Forbidden: You cannot delete another user's post"}), 403
        
    POSTS.remove(post)
    return jsonify({"success": True, "message": "Post deleted successfully"})
