import { upRegister, checkchunk, upChunk, mergeChunks } from '@/api/media';
import CryptoJS from 'crypto-js';

export const uploadByPieces = ({ file, pieceSize = 5, success, error }) => {
    let fileMD5 = "";
    const chunkSize = pieceSize * 1024 * 1024;
    const chunkCount = Math.ceil(file.size / chunkSize);

    const getChunkInfo = (file, currentChunk, chunkSize) => {
        let start = currentChunk * chunkSize;
        let end = Math.min(file.size, start + chunkSize);
        let chunk = file.raw.slice(start, end);
        return chunk;
    };

    const readFileMD5 = () => {
        const startChunk = getChunkInfo(file, 0, chunkSize);
        const endChunk = getChunkInfo(file, chunkCount - 1, chunkSize);
        let fileRederInstance = new FileReader();
        fileRederInstance.readAsBinaryString(file.raw);
        fileRederInstance.addEventListener("load", (e) => {
            let fileBolb = e.target.result;
            fileMD5 = CryptoJS.MD5(CryptoJS.enc.Latin1.parse(fileBolb)).toString();
            const params = {
                fileMd5: fileMD5
            };
            upRegister(params).then(res => {
                if (res.code === 0) {
                    readChunkMD5(0);
                }
            }).catch(err => error(err));
        });
    };

    const readChunkMD5 = async (num) => {
        if (num <= chunkCount - 1) {
            const chunk = getChunkInfo(file, num, chunkSize);
            await checkchunk({ fileMd5: fileMD5, chunk: num }).then(async res => {
                if (res.code === 0 && res.result === false) {
                    let fetchForm = new FormData();
                    fetchForm.append("file", chunk);
                    fetchForm.append("fileMd5", fileMD5);
                    fetchForm.append("chunk", num);
                    await upChunk(fetchForm).then(async res => {
                        success({ num, chunkCount, state: 'uploading' });
                        if (res.code === 0) {
                            readChunkMD5(num + 1);
                        }
                    }).catch(err => {
                        error(err);
                    });
                } else {
                    success({ num, chunkCount, state: 'uploading' });
                    readChunkMD5(num + 1);
                }
            });
        } else {
            mergeChunks({
                fileMd5: fileMD5,
                fileName: file.name,
                chunkTotal: chunkCount
            }).then(res => {
                success({ num, chunkCount, state: 'success' });
            }).catch(err => {
                error(err);
            });
        }
    };

    readFileMD5();
};