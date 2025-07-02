export async function convertWebMToWav(webmBlob) {
    const audioContext = new (window.AudioContext || window.webkitAudioContext)();
    const arrayBuffer = await webmBlob.arrayBuffer();
    const audioBuffer = await audioContext.decodeAudioData(arrayBuffer);

    // 创建WAV头部信息
    function createWavHeader(sampleRate, bitsPerSample, channels, dataSize) {
        const blockAlign = channels * (bitsPerSample / 8);
        const byteRate = sampleRate * blockAlign;
        const headerBytes = 44;
        const buffer = new ArrayBuffer(headerBytes);
        const dv = new DataView(buffer);

        writeString(dv, 0, 'RIFF'); // ChunkID
        dv.setUint32(4, 36 + dataSize, true); // ChunkSize
        writeString(dv, 8, 'WAVE'); // Format
        writeString(dv, 12, 'fmt '); // Subchunk1ID
        dv.setUint32(16, 16, true); // Subchunk1Size
        dv.setUint16(20, 1, true); // AudioFormat (PCM)
        dv.setUint16(22, channels, true); // NumChannels
        dv.setUint32(24, sampleRate, true); // SampleRate
        dv.setUint32(28, byteRate, true); // ByteRate
        dv.setUint16(32, blockAlign, true); // BlockAlign
        dv.setUint16(34, bitsPerSample, true); // BitsPerSample
        writeString(dv, 36, 'data'); // Subchunk2ID
        dv.setUint32(40, dataSize, true); // Subchunk2Size

        return buffer;

        function writeString(view, offset, string) {
            for (let i = 0; i < string.length; i++) {
                view.setUint8(offset + i, string.charCodeAt(i));
            }
        }
    }

    // 将AudioBuffer转换为Float32Array
    function interleave(input) {
        const buffer = input.getChannelData(0);
        const length = buffer.length;
        const result = new Float32Array(length);
        result.set(buffer);
        return result;
    }

    const interleaved = interleave(audioBuffer);
    const buffer = createWavHeader(audioBuffer.sampleRate, 16, audioBuffer.numberOfChannels, interleaved.length * 2);
    const wavBuffer = new Uint8Array(buffer.byteLength + interleaved.length * 2);

    // 复制WAV头部
    wavBuffer.set(new Uint8Array(buffer), 0);

    // 将PCM数据转换为16位并写入
    floatTo16BitPCM(wavBuffer, buffer.byteLength, interleaved);

    function floatTo16BitPCM(output, offset, input) {
        for (let i = 0; i < input.length; i++, offset += 2) {
            let s = Math.max(-1, Math.min(1, input[i]));
            console.log(output)
            output(offset, s < 0 ? s * 0x8000 : s * 0x7FFF, true);
        }
    }

    return new Blob([wavBuffer], { type: 'audio/wav' });
}