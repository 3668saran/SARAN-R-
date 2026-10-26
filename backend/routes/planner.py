from flask import Blueprint, request, jsonify
from backend.auth_service import require_auth
from backend.validators import sanitize_string
import time

planner_bp = Blueprint("planner", __name__, url_prefix="/api/planner")

TASKS = [
    {
        "id": "task-1",
        "userId": "user-std-101",
        "title": "Complete Operating Systems Lab Assignment 4",
        "subject": "Computer Science",
        "priority": "HIGH",
        "dueDate": "Tomorrow, 5:00 PM",
        "isCompleted": False
    },
    {
        "id": "task-2",
        "userId": "user-std-101",
        "title": "Review Discrete Math Chapter 5",
        "subject": "Mathematics",
        "priority": "MEDIUM",
        "dueDate": "Oct 28, 2026",
        "isCompleted": False
    }
]

@planner_bp.route("/tasks", methods=["GET"])
@require_auth
def get_tasks():
    uid = request.user.get("uid")
    user_tasks = [t for t in TASKS if t.get("userId") == uid]
    return jsonify({"success": True, "tasks": user_tasks})

@planner_bp.route("/tasks", methods=["POST"])
@require_auth
def add_task():
    data = request.get_json() or {}
    title = sanitize_string(data.get("title", ""), 200)
    subject = sanitize_string(data.get("subject", "General"), 50)
    priority = data.get("priority", "MEDIUM")
    due_date = sanitize_string(data.get("dueDate", "Tomorrow"), 50)
    
    if not title:
        return jsonify({"success": False, "error": "Title is required"}), 400
        
    new_task = {
        "id": f"task-{int(time.time())}",
        "userId": request.user.get("uid"),
        "title": title,
        "subject": subject,
        "priority": priority,
        "dueDate": due_date,
        "isCompleted": False
    }
    TASKS.insert(0, new_task)
    return jsonify({"success": True, "task": new_task}), 201

@planner_bp.route("/tasks/<task_id>/toggle", methods=["PUT"])
@require_auth
def toggle_task(task_id):
    uid = request.user.get("uid")
    task = next((t for t in TASKS if t["id"] == task_id and t["userId"] == uid), None)
    if not task:
        return jsonify({"success": False, "error": "Task not found"}), 404
        
    task["isCompleted"] = not task["isCompleted"]
    return jsonify({"success": True, "task": task})

@planner_bp.route("/tasks/<task_id>", methods=["DELETE"])
@require_auth
def delete_task(task_id):
    uid = request.user.get("uid")
    task = next((t for t in TASKS if t["id"] == task_id and t["userId"] == uid), None)
    if not task:
        return jsonify({"success": False, "error": "Task not found"}), 404
        
    TASKS.remove(task)
    return jsonify({"success": True, "message": "Task removed"})
