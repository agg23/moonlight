/* ======================================= */
/* Timer & Alarm logic (Demo #3)
   ---------------------------------- */

const timerButton = document.querySelector('.timer-button');
const alarmButton = document.querySelector('.alarm-button');
const timerToggle = document.querySelector('.timer-toggle');
const alarmToggle = document.querySelector('.alarm-toggle');
const overlay = document.querySelector('.overlay');
const overlayText = document.querySelector('.overlay-text');
const currentTime = document.querySelector('.time');

const timerTemplate = `
  <p class="timer secondary name">salmon</p>
  <p class="timer primary">17:35</p>
  <p class="timer secondary">20:00</p>
`;

const alarmTemplate = `
  <p class="alarm secondary next">next alarm</p>
  <p class="alarm primary">6:00 am</p>
  <p class="alarm secondary">Weekday</p>
`;

// Guard in case they're missing from the DOM
if (
  timerButton && alarmButton &&
  timerToggle && alarmToggle &&
  overlay && overlayText && currentTime
) {
  timerButton.addEventListener('mouseenter', () => {
    overlay.classList.remove('hidden');
    alarmButton.style.zIndex = 50;
    overlayText.innerHTML = timerToggle.checked ? timerTemplate : 'no timers set';
  });

  alarmButton.addEventListener('mouseenter', () => {
    overlay.classList.remove('hidden');
    timerButton.style.zIndex = 50;
    overlayText.innerHTML = alarmToggle.checked ? alarmTemplate : 'no alarms set';
  });

  timerButton.addEventListener('mouseleave', () => {
    overlay.classList.add('hidden');
    timerButton.style.zIndex = 150;
  });

  alarmButton.addEventListener('mouseleave', () => {
    overlay.classList.add('hidden');
    alarmButton.style.zIndex = 150;
  });

  timerToggle.addEventListener('change', () => {
    timerButton.classList.toggle('secondary', !timerToggle.checked);
  });

  alarmToggle.addEventListener('change', () => {
    alarmButton.classList.toggle('secondary', !alarmToggle.checked);
  });

  // Display local time (updates every 30s)
  function displayLocalTime() {
    const now = new Date();
    const hours = now.getHours() % 12 || 12;
    const minutes = now.getMinutes().toString().padStart(2, '0');
    const formattedTime = `${hours}:${minutes}`;
    currentTime.textContent = formattedTime;
  }

  displayLocalTime();
  setInterval(displayLocalTime, 30000);
}

/* ======================================= */
/* Magnetism effect with threshold
   Adjusted for a smaller range and added bounce
   ---------------------------------- */

/**
 * Utility for mapping a value from one range to another.
 * This converts a value within [inputLower, inputUpper] to one within [outputLower, outputUpper].
 */
const mapRange = (inputLower, inputUpper, outputLower, outputUpper) => {
  const INPUT_RANGE = inputUpper - inputLower;
  const OUTPUT_RANGE = outputUpper - outputLower;
  return (value) =>
    outputLower + (((value - inputLower) / INPUT_RANGE) * OUTPUT_RANGE || 0);
};

// Grab all buttons
const items = document.querySelectorAll("button");

const updateMagnet = (event) => {
  const item = event.currentTarget;
  // Disable inline transitions so the button follows the pointer instantly.
  item.style.transition = "none";

  // Calculate the difference between the button's center and the pointer.
  const dx = item.dataset.centerX - event.x;
  const dy = item.dataset.centerY - event.y;
  
  // Map the pixel difference to a value between 1 and -1.
  const xRange = item.magnetMapper.x(dx);
  const yRange = item.magnetMapper.y(dy);
  
  // Update the custom properties for the magnet effect.
  item.style.setProperty("--magnet-x", xRange);
  item.style.setProperty("--magnet-y", yRange);
};

const disableMagnet = (event) => {
  const item = event.currentTarget || event.target;
  // Remove the inline transition override so that the CSS-defined transition kicks in.
  item.style.transition = "";
  // Reset the custom properties so that CSS transitions smoothly animate back.
  item.style.setProperty("--magnet-x", 0);
  item.style.setProperty("--magnet-y", 0);
  
  item.removeEventListener("pointermove", updateMagnet);
  item.removeEventListener("pointerleave", disableMagnet);
};

const activateMagnet = (event) => {
  const item = event.target;
  const bounds = item.getBoundingClientRect();
  
  // Cache the center coordinates on pointer enter.
  item.dataset.centerX = bounds.x + item.offsetWidth * 0.5;
  item.dataset.centerY = bounds.y + item.offsetHeight * 0.5;
  
  if (!item.magnetMapper) {
    item.magnetMapper = {
      x: mapRange(item.offsetWidth * -0.5, item.offsetWidth * 0.5, 1, -1),
      y: mapRange(item.offsetHeight * -0.5, item.offsetHeight * 0.5, 1, -1)
    };
  }
  
  if (event.type === "pointerenter") {
    item.addEventListener("pointermove", updateMagnet);
    item.addEventListener("pointerleave", disableMagnet);
  }
};

items.forEach((item) => {
  item.addEventListener("pointerenter", activateMagnet);
  item.addEventListener("focus", activateMagnet);
});
