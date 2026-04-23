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
update();
setInterval(update, 1000);

// Parse locale-aware number input (accepts both German 10.000 and American 10,000 formats)
function parseLocaleNumber(str) {
    if (!str) return 0;
    // Detect format: if string contains both '.' and ',', last separator is likely thousands
    // German: 1.234.567 (dots for thousands), American: 1,234,567 (commas for thousands)
    // Strategy: find the last non-digit separator, assume it's thousands separator
    // The other separator (if exists) is decimal separator
    const cleanStr = str.trim();
    const lastDot = cleanStr.lastIndexOf('.');
    const lastComma = cleanStr.lastIndexOf(',');

    if (lastDot > lastComma) {
        // Dot is thousands separator (German style: 1.234.567)
        return Math.round(Number(cleanStr.replace(/\./g, '').replace(',', '.')));
    } else if (lastComma > lastDot) {
        // Comma is thousands separator (American style: 1,234,567)
        return Math.round(Number(cleanStr.replace(/,/g, '')));
    } else {
        // No thousands separator found, just parse
        return Math.round(Number(cleanStr));
    }
}

// Format number using user's locale
function formatLocaleNumber(value) {
    return Number(value).toLocaleString();
}

// Auto-submit numeric input forms on change
document.querySelectorAll('.inline-form').forEach(function(form) {
    const input = form.querySelector('input[type="text"]');
    if (!input) return;
    input.addEventListener('change', function(e) {
        const hiddenInput = form.querySelector('.dilithium-value, .credits-value');
        const value = parseLocaleNumber(input.value);
        hiddenInput.value = value;
        input.value = formatLocaleNumber(value);

        // Update dilithium multiplier
        const dilithiumCell = input.closest('.dilithium-cell');
        if (dilithiumCell) {
            const multiplierEl = dilithiumCell.querySelector('.dilithium-multiplier');
            if (multiplierEl) {
                const multiplier = Math.floor(value / 8000);
                multiplierEl.textContent = multiplier + 'x';
                multiplierEl.classList.remove('multiplier-high', 'multiplier-mid', 'multiplier-low');
                if (multiplier > 5) {
                    multiplierEl.classList.add('multiplier-high');
                } else if (multiplier >= 3) {
                    multiplierEl.classList.add('multiplier-mid');
                } else {
                    multiplierEl.classList.add('multiplier-low');
                }
            }
        }

        // Update credits warning
        const creditsCell = input.closest('.credits-cell');
        if (creditsCell) {
            const warningEl = creditsCell.querySelector('.credits-warning');
            if (warningEl) {
                let html = '';
                if (value < 100000) {
                    html = '<span class="warning-critical">🚨</span>';
                } else if (value < 1000000) {
                    html = '<span class="warning-low">⚠️</span>';
                }
                warningEl.innerHTML = html;
            }
        }

        form.submit();
    });
    input.addEventListener('blur', function() {
        const hiddenInput = form.querySelector('.dilithium-value, .credits-value');
        hiddenInput.value = parseLocaleNumber(input.value);
        input.value = formatLocaleNumber(hiddenInput.value);
    });
});
