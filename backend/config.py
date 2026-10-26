import os
from dotenv import load_dotenv

load_dotenv()

class Config:
    SECRET_KEY = os.getenv("FLASK_SECRET_KEY", "studymate_dev_secret_key_change_in_production")
    
    # Gemma / Gemini AI Model Settings
    GEMMA_API_KEY = os.getenv("GEMMA_API_KEY") or os.getenv("GEMINI_API_KEY") or ""
    GEMMA_MODEL = os.getenv("GEMMA_MODEL", "gemini-3.5-flash")
    
    # Firebase Settings
    FIREBASE_PROJECT_ID = os.getenv("FIREBASE_PROJECT_ID", "studymate-ai-demo")
    FIREBASE_ADMIN_CREDENTIALS_PATH = os.getenv("FIREBASE_ADMIN_CREDENTIALS_PATH", "serviceAccountKey.json")
    FIREBASE_STORAGE_BUCKET = os.getenv("FIREBASE_STORAGE_BUCKET", "studymate-ai-demo.firebasestorage.app")
    
    # CORS & Web Settings
    FRONTEND_ORIGIN = os.getenv("FRONTEND_ORIGIN", "*")
    PORT = int(os.getenv("PORT", 5000))
    DEBUG = os.getenv("FLASK_ENV") == "development"
