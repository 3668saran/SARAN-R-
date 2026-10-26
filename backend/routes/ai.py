from flask import Blueprint, request, jsonify
from backend.ai_service import GemmaAIService
from backend.auth_service import require_auth
from backend.validators import sanitize_string

ai_bp = Blueprint("ai", __name__, url_prefix="/api/ai")
ai_service = GemmaAIService()

@ai_bp.route("/chat", methods=["POST"])
@require_auth
def chat():
    data = request.get_json() or {}
    prompt = data.get("prompt", "")
    subject = sanitize_string(data.get("subject", "General"), 50)
    
    if not prompt or len(prompt.strip()) == 0:
        return jsonify({"success": False, "error": "Prompt is required"}), 400
        
    result = ai_service.ask_tutor(prompt, subject)
    return jsonify(result)

@ai_bp.route("/ask", methods=["POST"])
@require_auth
def ask():
    data = request.get_json() or {}
    question = data.get("question", "").strip()
    if not question or len(question) > 2000:
        return jsonify({"error": "Enter a question of up to 2000 characters."}), 400
    res = ai_service.ask_tutor(question)
    return jsonify({"answer": res.get("response", "Please try asking again.")})


@ai_bp.route("/generate-quiz", methods=["POST"])
@require_auth
def generate_quiz():
    data = request.get_json() or {}
    topic = data.get("topic", "")
    if not topic:
        return jsonify({"success": False, "error": "Topic is required"}), 400
        
    result = ai_service.generate_quiz(topic)
    return jsonify(result)

@ai_bp.route("/status", methods=["GET"])
def model_status():
    return jsonify({
        "status": "online",
        "model": ai_service.model,
        "features": ["academic_tutor", "quiz_generation", "trustcheck_precheck"]
    })
