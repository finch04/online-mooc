// azureTtsService.js
import axios from 'axios';

class AzureTtsService {
  constructor(subscriptionKey, region) {
    this.subscriptionKey = subscriptionKey;
    this.region = region;
    this.accessToken = null;
    this.baseUrl = `https://${this.region}.tts.speech.microsoft.com/cognitiveservices/v1`;
  }

  async getAccessToken() {
    const fetchTokenUrl = `https://${this.region}.api.cognitive.microsoft.com/sts/v1.0/issuetoken`;
    const headers = {
      'Ocp-Apim-Subscription-Key': this.subscriptionKey,
    };
    const response = await axios.post(fetchTokenUrl, null, { headers });
    this.accessToken = response.data;
  }

  async textToSpeech(text) {
    if (!this.accessToken) {
      await this.getAccessToken();
    }

    const ssml = `<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='zh-CN'>
      <voice name='zh-CN-XiaoxiaoNeural'>
        ${text}
      </voice>
    </speak>`;

    const headers = {
      'Authorization': `Bearer ${this.accessToken}`,
      'Content-Type': 'application/ssml+xml',
      'X-Microsoft-OutputFormat': 'riff-16khz-16bit-mono-pcm',
    };

    const response = await axios.post(this.baseUrl, ssml, { headers, responseType: 'blob' });
    return response.data;
  }
}

export default AzureTtsService;
