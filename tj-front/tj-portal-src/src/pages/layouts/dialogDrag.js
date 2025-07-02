export default {
    mounted(el) {
      const dialogHeaderEl = el.querySelector('.el-dialog__header');
      const dragDom = el.querySelector('.el-dialog');
  
      if (!dialogHeaderEl || !dragDom) return;
  
      // 只有头部可以触发拖动
      dialogHeaderEl.style.cursor = 'move';
  
      dialogHeaderEl.onmousedown = (e) => {
        // 检查点击的目标是否是 el-popover 的触发元素
        const isPopoverTrigger = e.target.closest('.el-popover__trigger');
        if (isPopoverTrigger) {
          // 如果是 el-popover 的触发元素，则不触发拖动
          return;
        }
  
        // 阻止事件冒泡，避免影响其他组件（如 el-popover）
        e.stopPropagation();
  
        const disX = e.clientX - dragDom.offsetLeft;
        const disY = e.clientY - dragDom.offsetTop;
  
        document.onmousemove = (e) => {
          let left = e.clientX - disX;
          let top = e.clientY - disY;
  
          // 限制拖动范围
          const maxLeft = window.innerWidth - dragDom.offsetWidth;
          const maxTop = window.innerHeight - dragDom.offsetHeight;
  
          left = Math.max(0, Math.min(left, maxLeft));
          top = Math.max(0, Math.min(top, maxTop));
  
          dragDom.style.left = `${left}px`;
          dragDom.style.top = `${top}px`;
        };
  
        document.onmouseup = () => {
          document.onmousemove = null;
          document.onmouseup = null;
        };
      };
    },
  };