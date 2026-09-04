import { useEffect, useState } from "react";

function App() {
  const [message, setMessage] = useState("Connecting to backend...");

  useEffect(() => {
    fetch("http://localhost:8080/api/health")
      .then((response) => response.text())
      .then((data) => setMessage(data))
      .catch(() => setMessage("Backend connection failed"));
  }, []);

  return (
    <div>
      <h1>Weekly Report System</h1>
      <p>{message}</p>
    </div>
  );
}

export default App;
