// Live clock updates every second (CET timezone)
function updateClock() {
    const now = new Date();
    const cetTime = new Date(now.toLocaleString('en-US', { timeZone: 'Europe/Berlin' }));
    const hours = String(cetTime.getHours()).padStart(2, '0');
    const minutes = String(cetTime.getMinutes()).padStart(2, '0');
    document.getElementById('clock').textContent = `${hours}:${minutes}`;
}

// Update immediately and then every second
updateClock();
setInterval(updateClock, 1000);

// Auto-submit numeric input forms on change
document.querySelectorAll('.inline-form').forEach(form => {
    const input = form.querySelector('input[type=number]');
    input.addEventListener('change', () => {
        // Submit the parent inline-form to /update
        const id = form.querySelector('input[name=id]').value;
        const isDilithium = form.classList.contains('dilithium-input') ||
                           input.classList.contains('dilithium-input');
        const isCredits = form.classList.contains('credits-input') ||
                          input.classList.contains('credits-input');

        const hiddenInput = form.querySelector(isDilithium ? '.dilithium-value' : '.credits-value');
        hiddenInput.value = input.value;

        form.submit();
    });
});