// Format number with German locale (dot as thousands separator)
function formatGermanNumber(value) {
    return Number(value).toLocaleString('de-DE');
}

// Parse German number input (accept 10.000 or 10,000 or 10000)
function parseGermanNumber(str) {
    if (!str) return 0;
    // Remove dots (German thousands) and replace comma with dot (German decimal)
    return Math.round(Number(str.replace(/\./g, '').replace(',', '.')));
}

// Initialize number inputs with German format on page load
function initNumberInputs() {
    document.querySelectorAll('.lcars-input[data-original-value]').forEach(input => {
        const originalValue = input.getAttribute('data-original-value');
        if (originalValue) {
            input.value = formatGermanNumber(originalValue);
        }
    });
}

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
    const cetTime = new Date(now.toLocaleString('en-US', { timeZone: 'Europe/Berlin' }));
    cetTime.setHours(2, 0, 0, 0);
    if (cetTime.getTime() <= now.getTime()) {
        cetTime.setDate(cetTime.getDate() + 1);
    }
    return cetTime;
}

// Parse timestamp as CET (server stores times in CET but sends as UTC strings)
function parseCET(timestamp) {
    // Server sends: "2026-04-23T11:20:32.173" which is UTC time (13:20 CET = 11:20 UTC)
    // We need to add 2 hours to convert UTC back to CET
    const date = new Date(timestamp);
    // CET is UTC+2 in summer
    return new Date(date.getTime() + (2 * 60 * 60 * 1000));
}

// Update countdown timers
function updateCountdowns() {
    document.querySelectorAll('.countdown').forEach(el => {
        const timestamp = el.getAttribute('data-timestamp');
        const type = el.getAttribute('data-type');
        if (!timestamp || !type) return;

        const lastUpdated = parseCET(timestamp);
        let targetTime;
        let diff;

        if (type === 'recruitment') {
            // Recruitment: 20 minutes from last updated
            targetTime = new Date(lastUpdated.getTime() + 20 * 60 * 1000);
            diff = targetTime - new Date();
        } else if (type === 'refining' || type === 'event') {
            // Refining/Event: countdown to 02:00 tomorrow CET
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
initNumberInputs();
update();
setInterval(update, 1000);

// Auto-submit numeric input forms on change
document.querySelectorAll('.inline-form').forEach(form => {
    const input = form.querySelector('input[type="text"]');
    if (!input) return;
    input.addEventListener('change', () => {
        const hiddenInput = form.querySelector('.dilithium-value, .credits-value');
        hiddenInput.value = parseGermanNumber(input.value);
        input.value = formatGermanNumber(hiddenInput.value);
        form.submit();
    });
});