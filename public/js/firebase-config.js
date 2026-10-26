// Firebase Web Client Configuration Template
// Replace placeholders with your Firebase Console Web App config values.

const firebaseConfig = {
  apiKey: "AIzaSy_YOUR_FIREBASE_WEB_API_KEY",
  authDomain: "studymate-ai-demo.firebaseapp.com",
  projectId: "studymate-ai-demo",
  storageBucket: "studymate-ai-demo.firebasestorage.app",
  messagingSenderId: "1234567890",
  appId: "1:1234567890:web:abcdef123456"
};

// Check if Firebase SDK is included via CDN
if (typeof firebase !== 'undefined') {
  try {
    firebase.initializeApp(firebaseConfig);
    console.log("Firebase Web SDK initialized.");
  } catch (e) {
    console.warn("Firebase Web SDK auto-init notice:", e);
  }
}
