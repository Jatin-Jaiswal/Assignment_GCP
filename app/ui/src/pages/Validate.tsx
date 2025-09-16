import { useEffect, useState } from 'react';

export default function Validate() {
  const [loading, setLoading] = useState(false);
  const [items, setItems] = useState<any[]>([]);
  const [status, setStatus] = useState<string>("");

  async function triggerSample() {
    setLoading(true);
    setStatus("Queuing sample...");
    try {
      const res = await fetch('/v1/validate/sample', { method: 'POST' });
      const data = await res.json();
      setStatus(res.ok ? `Queued id=${data.id}` : `Error: ${data.error}`);
      await fetchList();
    } catch (e: any) {
      setStatus(`Error: ${e?.message || e}`);
    } finally {
      setLoading(false);
    }
  }

  async function fetchList() {
    try {
      const res = await fetch('/v1/validate/list');
      const data = await res.json();
      setItems(data.items || []);
    } catch {}
  }

  useEffect(() => { fetchList(); }, []);

  return (
    <div style={{ padding: 20 }}>
      <h2>Validate</h2>
      <button onClick={triggerSample} disabled={loading}>
        {loading ? 'Working...' : 'Publish Sample'}
      </button>
      <p>{status}</p>
      <h3>Recent Validated Objects</h3>
      <table border={1} cellPadding={6}>
        <thead>
          <tr>
            <th>Name</th>
            <th>Size</th>
            <th>Updated</th>
          </tr>
        </thead>
        <tbody>
          {items.map((it, idx) => (
            <tr key={idx}>
              <td>{it.name}</td>
              <td>{it.size}</td>
              <td>{it.updated}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

