import { Link } from 'react-router-dom'

function App() {
  return (
    <div style={{ padding: 20 }}>
      <h1>Autovyn</h1>
      <ul>
        <li><Link to="/">Home</Link></li>
        <li><Link to="/validate">Validate</Link></li>
      </ul>
      <p>Welcome to Autovyn UI</p>
    </div>
  )
}

export default App


