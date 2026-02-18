(() => {
  function initReveal() {
    const sections = document.querySelectorAll('.reveal');
    if (!('IntersectionObserver' in window)) {
      sections.forEach((section) => section.classList.add('is-visible'));
      return;
    }

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('is-visible');
            observer.unobserve(entry.target);
          }
        });
      },
      { threshold: 0.12 }
    );

    sections.forEach((section) => observer.observe(section));
  }

  function initHistory() {
    const items = Array.isArray(window.QMA_UPDATE_HISTORY) ? window.QMA_UPDATE_HISTORY : [];
    const list = document.getElementById('updateHistoryList');
    const count = document.getElementById('historyCount');
    const moreButton = document.getElementById('historyMore');
    const allButton = document.getElementById('historyAll');

    if (!list || !count || !moreButton || !allButton) {
      return;
    }

    const pageSize = 20;
    let visible = Math.min(pageSize, items.length);

    function render() {
      list.innerHTML = '';
      const shown = items.slice(0, visible);

      shown.forEach((item) => {
        const details = document.createElement('details');
        details.className = 'history-item';

        const summary = document.createElement('summary');

        const meta = document.createElement('span');
        meta.className = 'history-meta';
        meta.textContent = `${item.date} | v${item.version}`;

        const label = document.createElement('span');
        label.className = 'history-label';
        const shortText = item.detail.length > 44 ? `${item.detail.slice(0, 44)}...` : item.detail;
        label.textContent = shortText;

        summary.appendChild(meta);
        summary.appendChild(label);

        const body = document.createElement('p');
        body.className = 'history-detail';

        const bulletParts = item.detail
          .split('・')
          .map((part) => part.trim())
          .filter((part) => part.length > 0);

        if (item.detail.includes('・') && bulletParts.length > 0) {
          const leadText = item.detail.trim().startsWith('・') ? '' : bulletParts.shift() || '';
          if (leadText) {
            const lead = document.createElement('p');
            lead.className = 'history-detail-lead';
            lead.textContent = leadText;
            body.appendChild(lead);
          }

          if (bulletParts.length > 0) {
            const list = document.createElement('ul');
            list.className = 'history-detail-list';
            bulletParts.forEach((part) => {
              const li = document.createElement('li');
              li.textContent = part;
              list.appendChild(li);
            });
            body.appendChild(list);
          }
        } else {
          const text = document.createElement('p');
          text.className = 'history-detail-lead';
          text.textContent = item.detail;
          body.appendChild(text);
        }

        details.appendChild(summary);
        details.appendChild(body);
        list.appendChild(details);
      });

      count.textContent = `${visible} / ${items.length} 件を表示`;
      moreButton.hidden = visible >= items.length;
      allButton.hidden = visible >= items.length;
    }

    moreButton.addEventListener('click', () => {
      visible = Math.min(visible + pageSize, items.length);
      render();
    });

    allButton.addEventListener('click', () => {
      visible = items.length;
      render();
    });

    render();
  }

  initReveal();
  initHistory();
})();
