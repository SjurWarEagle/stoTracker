// Row highlighting - click on name cell to highlight entire row
// Only one row highlighted at a time, persisted in sessionStorage
const HIGHLIGHT_KEY = 'sto-highlighted-name';

function getHighlightedName() {
    return sessionStorage.getItem(HIGHLIGHT_KEY);
}

function restoreHighlight() {
    const highlightedName = getHighlightedName();
    if (!highlightedName) return;

    document.querySelectorAll('.name-cell').forEach(cell => {
        if (cell.textContent.trim() === highlightedName) {
            // Add highlighted class to the parent row
            const row = cell.closest('.grid-row');
            if (row) {
                row.classList.add('highlighted');
            }
        }
    });
}

function toggleRowHighlight(nameCell) {
    const row = nameCell.closest('.grid-row');
    const name = nameCell.textContent.trim();

    // If clicking already-highlighted row, remove highlight
    if (row && row.classList.contains('highlighted')) {
        row.classList.remove('highlighted');
        sessionStorage.removeItem(HIGHLIGHT_KEY);
        return;
    }

    // Remove highlight from all other rows
    document.querySelectorAll('.grid-row.highlighted').forEach(r => {
        r.classList.remove('highlighted');
    });

    // Add highlight to clicked row and persist
    if (row) {
        row.classList.add('highlighted');
        sessionStorage.setItem(HIGHLIGHT_KEY, name);
    }
}

// Live clock updates every second (CET timezone)
function updateClock() {
    const now = new Date();
    const cetTime = new Date(now.toLocaleString('en-US', { timeZone: 'Europe/Berlin' }));
    const hours = String(cetTime.getHours()).padStart(2, '0');
    const minutes = String(cetTime.getMinutes()).padStart(2, '0');
    document.getElementById('clock').textContent = `${hours}:${minutes}`;
}

// Get current time in CET timezone
function getCurrentCET() {
    const now = new Date();
    // Get current Berlin time components
    const berlinStr = now.toLocaleString('en-US', { timeZone: 'Europe/Berlin' });
    const parts = berlinStr.match(/(\d+)\/(\d+)\/(\d+), (\d+):(\d+):(\d+)\s*(AM|PM)/);
    let [, month, day, year, hours, minutes, seconds, ampm] = parts;
    month = parseInt(month);
    day = parseInt(day);
    year = parseInt(year);
    hours = parseInt(hours);
    minutes = parseInt(minutes);
    seconds = parseInt(seconds);
    if (ampm === 'PM' && hours !== 12) hours += 12;
    if (ampm === 'AM' && hours === 12) hours = 0;
    return new Date(Date.UTC(year, month - 1, day, hours, minutes, seconds));
}

// Get next 02:00 CET after the given timestamp's date
// If timestamp is before 02:00 CET, deadline is 02:00 that same day
// If timestamp is at or after 02:00 CET, deadline is 02:00 next day
function getNext02_00CETAfter(timestampCET) {
    const baseTime = new Date(timestampCET);
    baseTime.setUTCHours(2, 0, 0, 0);
    if (baseTime.getTime() <= timestampCET.getTime()) {
        baseTime.setUTCDate(baseTime.getUTCDate() + 1);
    }
    return baseTime;
}

// Parse timestamp as CET (server stores times in CET but sends as UTC strings)
// Properly handles DST by extracting Berlin time components and creating a UTC date
function parseCET(timestamp) {
    const date = new Date(timestamp);
    // Format the date in Berlin timezone to get local components
    const berlinStr = date.toLocaleString('en-US', { timeZone: 'Europe/Berlin' });
    // Parse the components (month, day, year, hours, minutes, seconds)
    const parts = berlinStr.match(/(\d+)\/(\d+)\/(\d+), (\d+):(\d+):(\d+)\s*(AM|PM)/);
    let [, month, day, year, hours, minutes, seconds, ampm] = parts;
    month = parseInt(month);
    day = parseInt(day);
    year = parseInt(year);
    hours = parseInt(hours);
    minutes = parseInt(minutes);
    seconds = parseInt(seconds);
    if (ampm === 'PM' && hours !== 12) hours += 12;
    if (ampm === 'AM' && hours === 12) hours = 0;
    // Create a UTC date using the Berlin time components
    // This preserves the correct CET/CEST time regardless of browser timezone
    return new Date(Date.UTC(year, month - 1, day, hours, minutes, seconds));
}

// Update countdown timers
function updateCountdowns() {
    const nowCET = getCurrentCET();
    document.querySelectorAll('.countdown').forEach(el => {
        const timestamp = el.getAttribute('data-timestamp');
        const type = el.getAttribute('data-type');
        if (!timestamp || !type) return;

        const lastUpdated = parseCET(timestamp);
        let targetTime;
        let diff;
        let isOverdue = false;

        if (type === 'recruitment') {
            // Recruitment: 20 minutes from last updated
            targetTime = new Date(lastUpdated.getTime() + 20 * 60 * 1000);
            diff = targetTime - nowCET;
        } else if (type === 'refining' || type === 'event') {
            // Refining/Event: deadline is 02:00 CET after the timestamp's date
            // If that deadline has passed, it's overdue
            targetTime = getNext02_00CETAfter(lastUpdated);
            diff = targetTime - nowCET;
            if (diff < 0) {
                isOverdue = true;
            }
        }

        if (isOverdue) {
            el.textContent = 'overdue';
            el.classList.add('overdue');
        } else if (diff <= 0) {
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
    restoreHighlight();
}
update();
setInterval(update, 1000);

// Simple number parsing (no locale-specific formatting)
function parseSimpleNumber(str) {
    if (!str) return null;
    const num = Number(str.trim());
    return isNaN(num) ? null : Math.round(num);
}

// Simple number formatting (no locale-specific formatting)
function formatSimpleNumber(value) {
    return Number(value).toString();
}

// Auto-submit numeric input forms on change
document.querySelectorAll('.inline-form').forEach(function(form) {
    const input = form.querySelector('input[type="text"]');
    if (!input) return;
input.addEventListener('change', function(e) {
        const hiddenInput = form.querySelector('.dilithium-value, .credits-value');
        const value = parseSimpleNumber(input.value);
        if (value === null) return; // Invalid input, don't submit
        hiddenInput.value = value;
        input.value = formatSimpleNumber(value);

        // Update dilithium multiplier
        const dilithiumCell = input.closest('.dilithium-cell');
        if (dilithiumCell) {
            const multiplierEl = dilithiumCell.querySelector('.lcars-badge');
            if (multiplierEl) {
                const multiplier = Math.floor(value / 8000);
                multiplierEl.textContent = multiplier + 'x';
                multiplierEl.classList.remove('lcars-badge-high', 'lcars-badge-mid', 'lcars-badge-low');
                if (multiplier > 5) {
                    multiplierEl.classList.add('lcars-badge-high');
                } else if (multiplier >= 3) {
                    multiplierEl.classList.add('lcars-badge-mid');
                } else {
                    multiplierEl.classList.add('lcars-badge-low');
                }
            }
        }

        // Update credits warning
        const creditsCell = input.closest('.credits-cell');
        if (creditsCell) {
            const warningEl = creditsCell.querySelector('.credits-warning');
            if (warningEl) {
                warningEl.textContent = '';
                if (value < 100000) {
                    warningEl.appendChild(document.createTextNode('🚨'));
                } else if (value < 1000000) {
                    warningEl.appendChild(document.createTextNode('⚠️'));
                }
            }
        }

        form.submit();
    });
    input.addEventListener('blur', function() {
        const hiddenInput = form.querySelector('.dilithium-value, .credits-value');
        hiddenInput.value = parseSimpleNumber(input.value);
        input.value = formatSimpleNumber(hiddenInput.value);
    });
});