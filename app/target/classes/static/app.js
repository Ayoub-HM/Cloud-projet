const apiRoot = '/api/notes';

async function listNotes() {
  const res = await fetch(apiRoot);
  if (!res.ok) return [];
  return res.json();
}

function noteItem(note) {
  const li = document.createElement('li');
  li.className = 'note';
  li.innerHTML = `<h3>${escapeHtml(note.title)}</h3><p>${escapeHtml(note.content)}</p><button data-id="${note.id}" class="delete">Delete</button>`;
  li.querySelector('.delete').addEventListener('click', async () => {
    await fetch(`${apiRoot}/${note.id}`, { method: 'DELETE' });
    render();
  });
  return li;
}

function escapeHtml(s){
  return String(s).replace(/[&<>"'`]/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":"&#39;","`":"&#96;"}[c]));
}

async function createNote(title, content){
  const res = await fetch(apiRoot, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ title, content })
  });
  return res.ok ? res.json() : null;
}

async function render(){
  const ul = document.getElementById('notes');
  ul.innerHTML = '';
  const notes = await listNotes();
  if (!notes || notes.length === 0) {
    ul.innerHTML = '<li class="empty">No notes yet.</li>';
    return;
  }
  notes.forEach(n => ul.appendChild(noteItem(n)));
}

document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('createForm');
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const title = document.getElementById('title').value.trim();
    const content = document.getElementById('content').value.trim();
    if (!title || !content) return;
    await createNote(title, content);
    form.reset();
    render();
  });
  render();
});
