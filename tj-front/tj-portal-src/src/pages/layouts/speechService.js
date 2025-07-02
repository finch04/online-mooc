import * as sdk from "microsoft-cognitiveservices-speech-sdk";

// import { SpeechSDK } from '@azure/cognitiveservices-speech-sdk';
const subscriptionKey = "F2AI7F2NLqIkmrB5XNakWPwDkEpMK73FAEiqsZtIVDWJ3PlVNF2vJQQJ99ALAC3pKaRXJ3w3AAAYACOGoc2q";
const serviceRegion = "eastasia"; // e.g., "westus"

export const recognizeSpeech = (audioConfig) => {
    console.log(audioConfig)
    
    // const speechConfig = sdk.SpeechConfig.fromSubscription(subscriptionKey, serviceRegion);

    // // 设置语言
    // speechConfig.speechRecognitionLanguage = "zh-CN"; // 例如中文

    // const recognizer = new sdk.SpeechRecognizer(speechConfig, audioConfig);

    // return new Promise((resolve, reject) => {
    //     recognizer.recognizeOnceAsync(
    //         (result) => {
    //             recognizer.close();
    //             if (result.reason === sdk.ResultReason.RecognizedSpeech) {
    //                 resolve(result.text);
    //             } else {
    //                 reject(new Error(`Recognition failed: ${result.reason}`));
    //             }
    //         },
    //         (err) => {
    //             recognizer.close();
    //             reject(err);
    //         }
    //     );
    // });
};