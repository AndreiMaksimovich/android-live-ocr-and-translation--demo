# Android Offline Live OCR &amp; Translation with ML Kit and Tesseract

This repository is a technical demo showcasing offline live **OCR (Optical Character Recognition)** and **translation** on **Android**.

It combines **Google ML Kit Text Recognition V2** for high-quality OCR with **Tesseract** OCR as a fallback, and uses **Google ML Kit Translation** for live, offline, on-device translation.

![Video](./video.mp4)

### Technologies Used:
* Google ML Kit Text Recognition V2: https://developers.google.com/ml-kit/vision/text-recognition/v2
* Tesseract OCR: https://github.com/tesseract-ocr
* Tesseract4Android: https://github.com/adaptech-cz/Tesseract4Android
* Google ML Kit Translation: https://developers.google.com/ml-kit/language/translation

### Installation
If required, create a **local.properties** file and specify the path to your Android SDK.

### Important Note
**This is a technical demo:**   
* Tesseract data files are stored in the assets folder.
* ML Kit language models are automatically downloaded during app initialization.
* Supported languages are hardcoded.
* The camera captures images without applying any filters or effects.

In a production application, you should use proper systems and managers to handle on-demand model downloads and dynamic language support.

The OCR workflow should begin with image preprocessing, including steps such as adjusting light balance, converting to grayscale or black-and-white, and applying region filtering.
