from flask import Blueprint, request, jsonify
from backend.auth_service import require_auth
import time

quizzes_bp = Blueprint("quizzes", __name__, url_prefix="/api/quizzes")

QUIZZES = [
    {
        "id": "quiz-py-1",
        "title": "Python Core & Data Structures",
        "subject": "Computer Science",
        "difficulty": "Intermediate",
        "questions": [
            {
                "id": "q1",
                "question": "Which of the following data structures in Python is immutable?",
                "options": ["List", "Dictionary", "Tuple", "Set"],
                "correctIndex": 2,
                "explanation": "Tuples in Python cannot be modified once instantiated."
            },
            {
                "id": "q2",
                "question": "What is the average time complexity for searching a key in a Python dict?",
                "options": ["O(1)", "O(log N)", "O(N)", "O(N²)"],
                "correctIndex": 0,
                "explanation": "Dictionaries use hash tables, providing O(1) average lookup."
            }
        ]
    }
]

ATTEMPTS = []
LEADERBOARD = [
    {"rank": 1, "name": "Samantha Miller", "points": 580, "verified": True},
    {"rank": 2, "name": "Alex Morgan", "points": 340, "verified": True},
    {"rank": 3, "name": "David Zhang", "points": 310, "verified": True}
]

@quizzes_bp.route("", methods=["GET"])
def list_quizzes():
    # Return questions without exposing correctIndex to prevent client-side inspection before submission
    safe_quizzes = []
    for q in QUIZZES:
        safe_q = {
            "id": q["id"],
            "title": q["title"],
            "subject": q["subject"],
            "difficulty": q["difficulty"],
            "questions": [
                {"id": item["id"], "question": item["question"], "options": item["options"]}
                for item in q["questions"]
            ]
        }
        safe_quizzes.append(safe_q)
    return jsonify({"success": True, "quizzes": safe_quizzes})

@quizzes_bp.route("/<quiz_id>/submit", methods=["POST"])
@require_auth
def submit_quiz(quiz_id):
    data = request.get_json() or {}
    answers = data.get("answers", {})  # { question_index: selected_option_index }
    
    quiz = next((q for q in QUIZZES if q["id"] == quiz_id), None)
    if not quiz:
        return jsonify({"success": False, "error": "Quiz not found"}), 404
        
    score = 0
    total = len(quiz["questions"])
    details = []
    
    for idx, q in enumerate(quiz["questions"]):
        user_choice = answers.get(str(idx), answers.get(idx))
        is_correct = (user_choice == q["correctIndex"])
        if is_correct:
            score += 1
        details.append({
            "questionIndex": idx,
            "userChoice": user_choice,
            "correctIndex": q["correctIndex"],
            "isCorrect": is_correct,
            "explanation": q["explanation"]
        })
        
    user = request.user
    attempt = {
        "id": f"att-{int(time.time())}",
        "userId": user.get("uid"),
        "quizTitle": quiz["title"],
        "score": score,
        "total": total,
        "date": "Today"
    }
    ATTEMPTS.insert(0, attempt)
    
    return jsonify({
        "success": True,
        "score": score,
        "total": total,
        "percentage": round((score / total) * 100),
        "details": details
    })

@quizzes_bp.route("/leaderboard", methods=["GET"])
def get_leaderboard():
    return jsonify({"success": True, "leaderboard": LEADERBOARD})
