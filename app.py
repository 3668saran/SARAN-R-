import os
from flask import Flask, jsonify, send_from_directory
from flask_cors import CORS
from backend.config import Config
from backend.firebase_service import init_firebase

from backend.routes.ai import ai_bp
from backend.routes.community import community_bp
from backend.routes.skills import skills_bp
from backend.routes.quizzes import quizzes_bp
from backend.routes.planner import planner_bp
from backend.routes.trustcheck import trustcheck_bp
from backend.routes.profile import profile_bp

def create_app():
    app = Flask(__name__, static_folder="public", static_url_path="")
    app.config.from_object(Config)
    
    # Configure CORS
    CORS(app, resources={r"/api/*": {"origins": Config.FRONTEND_ORIGIN}})
    
    # Initialize Firebase services
    init_firebase()
    
    # Register API Blueprints
    app.register_blueprint(ai_bp)
    app.register_blueprint(community_bp)
    app.register_blueprint(skills_bp)
    app.register_blueprint(quizzes_bp)
    app.register_blueprint(planner_bp)
    app.register_blueprint(trustcheck_bp)
    app.register_blueprint(profile_bp)
    
    @app.route("/health", methods=["GET"])
    def health():
        return jsonify({
            "status": "healthy",
            "app": "StudyMate AI",
            "version": "1.0.0",
            "gemmaModel": Config.GEMMA_MODEL
        })

    @app.route("/", methods=["GET"])
    def index():
        return send_from_directory(app.static_folder, "index.html")
        
    @app.route("/<path:filename>", methods=["GET"])
    def serve_public(filename):
        if os.path.exists(os.path.join(app.static_folder, filename)):
            return send_from_directory(app.static_folder, filename)
        return send_from_directory(app.static_folder, "index.html")

    return app

app = create_app()

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=Config.PORT, debug=Config.DEBUG)
