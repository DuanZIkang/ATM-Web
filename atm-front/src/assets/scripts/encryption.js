// src/assets/scripts/encryption.js
import CryptoJS from "crypto-js";

// ✅ 从 Vite 环境变量读取，.env 文件里配置 VITE_AES_KEY=your-secret-key!
const SECRET_KEY = import.meta.env.VITE_AES_KEY || "your-secret-key!";

export function encryptPassword(password) {
    const key = CryptoJS.enc.Utf8.parse(SECRET_KEY);

    const encrypted = CryptoJS.AES.encrypt(
        CryptoJS.enc.Utf8.parse(password),
        key,
        {
            mode: CryptoJS.mode.ECB,
            padding: CryptoJS.pad.Pkcs7
        }
    );

    return encrypted.toString(); // Base64
}