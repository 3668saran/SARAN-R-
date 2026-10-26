import os
import logging
from backend.config import Config

logger = logging.getLogger(__name__)

db = None
storage_bucket = None
_firebase_initialized = False

def init_firebase():
    global db, storage_bucket, _firebase_initialized
    if _firebase_initialized:
        return db, storage_bucket
        
    try:
        import firebase_admin
        from firebase_admin import credentials, firestore, storage
        
        cred_path = Config.FIREBASE_ADMIN_CREDENTIALS_PATH
        if os.path.exists(cred_path):
            cred = credentials.Certificate(cred_path)
            firebase_admin.initialize_app(cred, {
                'storageBucket': Config.FIREBASE_STORAGE_BUCKET,
                'projectId': Config.FIREBASE_PROJECT_ID
            })
            db = firestore.client()
            try:
                storage_bucket = storage.bucket()
            except Exception as e:
                logger.warning(f"Storage bucket init warning: {e}")
            _firebase_initialized = True
            logger.info("Firebase Admin initialized with service account.")
        else:
            logger.warning(f"Firebase credentials not found at {cred_path}. Operating with safe development store.")
    except Exception as e:
        logger.warning(f"Could not connect to Firebase Admin SDK: {e}. Falling back to dev state.")
        
    return db, storage_bucket

def get_firestore_db():
    global db
    if db is None:
        init_firebase()
    return db
