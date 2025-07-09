async function sendMessage() {
  const text = document.getElementById("message").value;
  await fetch("http://127.0.0.1:8000/message", {
    method: "POST",
    headers: { "Content-Type": "text/plain" },
    body: text
  });
  document.getElementById("message").value = "";
  loadMessages();
}

async function loadMessages() {
  const res = await fetch("http://127.0.0.1:8000/messages");
  const data = await res.json();
  const list = document.getElementById("messages");
  list.innerHTML = "";
  data.forEach(msg => {
    const li = document.createElement("li");
    li.textContent = msg;
    list.appendChild(li);
  });
}

window.onload = loadMessages;
