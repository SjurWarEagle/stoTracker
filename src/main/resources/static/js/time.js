// Live clock updates every second (CET timezone)
function updateClock() {
    const now = new Date();
    const cetTime = new Date(now.toLocaleString('en-US', { timeZone: 'Europe/Berlin' }));
    const hours = String(cetTime.getHours()).padStart(2, '0');
    const minutes = String(cetTime.getMinutes()).padStart(2, '0');
    document.getElementById('clock').textContent = `${hours}:${minutes}`;
}

// Get next 02:00 in CET
function getNext02_00CET() {
    const now = new Date();
    const cet = new Date(now.toLocaleString('en-US', { timeZone: 'Europe/Berlin' }));
    cet.setHours(2, 0, 0, 0);
    if (cet.getTime() <= now.getTime()) {
        cet.setDate(cet.getDate() + 1);
    }
    return cet;
}

// Update countdown timers
function updateCountdowns() {
    document.querySelectorAll('.countdown').forEach(el => {
        const timestamp = el.getAttribute('data-timestamp');
        const type = el.getAttribute('data-type');
        if (!timestamp || !type) return;

        const lastUpdated = new Date(timestamp);
        let targetTime;
        let diff;

        if (type === 'recruitment') {
            // Recruitment: 20 minutes from last updated
            targetTime = new Date(lastUpdated.getTime() + 20 * 60 * 1000);
            diff = targetTime - new Date();
        } else if (type === 'refining' || type === 'event') {
            // Refining/Event: countdown to 02:00 tomorrow
            targetTime = getNext02_00CET();
            diff = targetTime - new Date();
        }

        if (diff <= 0) {
            el.textContent = '0h 0m';
            el.classList.remove('overdue');
        } else {
            const hours = Math.floor(diff / (1000 * 60 * 60));
            const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
            el.textContent = `${hours}h ${minutes}m`;
            el.classList.remove('overdue');
        }
    });
}

// Update immediately and then every second
function update() {
    updateClock();
    updateCountdowns();
}
update();
setInterval(update, 1000);

// Auto-submit numeric input forms on change
document.querySelectorAll('.inline-form').forEach(form => {
    const input = form.querySelector('input[type="text"]');
    if (!input) return;
    input.addEventListener('change', () => {
        const hiddenInput = form.querySelector('.dilithium-value, .credits-value');
        hiddenInput.value = input.value.replace(/\./g, '').replace(',', '.');
        form.submit();
    });
});