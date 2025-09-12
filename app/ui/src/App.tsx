import React, { useMemo, useState } from 'react'
import axios from 'axios'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

type Item = {
  id: string
  name: string
  description?: string
  createdAt: string
  updatedAt: string
}

const fetchItems = async (): Promise<Item[]> => {
  const { data } = await axios.get('/v1/items')
  return data
}

export default function App() {
  const qc = useQueryClient()
  const { data: items = [] } = useQuery({ queryKey: ['items'], queryFn: fetchItems })
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [file, setFile] = useState<File | null>(null)

  const createMutation = useMutation({
    mutationFn: async () => {
      await axios.post('/v1/items', { name, description })
    },
    onSuccess: () => {
      setName(''); setDescription('')
      qc.invalidateQueries({ queryKey: ['items'] })
    }
  })

  const deleteMutation = useMutation({
    mutationFn: async (id: string) => {
      await axios.delete(`/v1/items/${id}`)
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: ['items'] })
  })

  const uploadMutation = useMutation({
    mutationFn: async () => {
      if (!file) return
      const form = new FormData()
      form.append('file', file)
      const { data } = await axios.post('/v1/files', form)
      return data as { downloadUrl: string }
    }
  })

  const isCreateDisabled = useMemo(() => !name.trim(), [name])

  return (
    <div style={{ fontFamily: 'system-ui, sans-serif', padding: 24, maxWidth: 1000, margin: '0 auto' }}>
      <header style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <h1>Items</h1>
        <HealthBadge />
      </header>

      <section style={{ marginTop: 24, display: 'grid', gap: 16, gridTemplateColumns: '1fr 1fr' }}>
        <div style={{ border: '1px solid #eee', borderRadius: 8, padding: 16 }}>
          <h3>Create Item</h3>
          <input placeholder="Name" value={name} onChange={e => setName(e.target.value)} style={{ width: '100%', padding: 8, marginBottom: 8 }} />
          <textarea placeholder="Description" value={description} onChange={e => setDescription(e.target.value)} style={{ width: '100%', padding: 8, marginBottom: 8 }} />
          <button disabled={isCreateDisabled || createMutation.isPending} onClick={() => createMutation.mutate()}>Create</button>
        </div>

        <div style={{ border: '1px solid #eee', borderRadius: 8, padding: 16 }}>
          <h3>Upload File</h3>
          <input type="file" onChange={e => setFile(e.target.files?.[0] || null)} />
          <button style={{ marginLeft: 8 }} onClick={() => uploadMutation.mutate()} disabled={!file || uploadMutation.isPending}>Upload</button>
          {uploadMutation.data && (
            <div style={{ marginTop: 8 }}>
              <a href={uploadMutation.data.downloadUrl} target="_blank">Download link</a>
            </div>
          )}
        </div>
      </section>

      <section style={{ marginTop: 24 }}>
        <h3>Items</h3>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr>
              <th style={{ textAlign: 'left', borderBottom: '1px solid #ddd', padding: 8 }}>Name</th>
              <th style={{ textAlign: 'left', borderBottom: '1px solid #ddd', padding: 8 }}>Description</th>
              <th style={{ textAlign: 'left', borderBottom: '1px solid #ddd', padding: 8 }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {items.map(it => (
              <tr key={it.id}>
                <td style={{ padding: 8 }}>{it.name}</td>
                <td style={{ padding: 8 }}>{it.description}</td>
                <td style={{ padding: 8 }}>
                  <button onClick={() => deleteMutation.mutate(it.id)}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  )
}

function HealthBadge() {
  const { data: live } = useQuery({ queryKey: ['live'], queryFn: async () => (await axios.get('/health/live')).data })
  const { data: ready } = useQuery({ queryKey: ['ready'], queryFn: async () => (await axios.get('/health/ready')).data })
  const ok = !!live && !!ready
  return (
    <span style={{ padding: '6px 10px', borderRadius: 6, background: ok ? '#e6ffed' : '#fff5f5', color: ok ? '#137333' : '#b00020' }}>
      {ok ? 'Healthy' : 'Degraded'}
    </span>
  )
}


