import mitt from 'mitt'

//事件发送
const emitter = mitt(); 

function getEmitter(){
    return emitter
}

export {getEmitter}