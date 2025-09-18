// src/pages/Signup.js
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

export default function Signup() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [isError, setIsError] = useState(false);

  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage("");
    setIsError(false);

    try {
      const response = await axios.post("http://localhost:9015/api/signup", {
        username,
        password,
      });

      const data = response.data;

      if (data.userId) {
        setMessage("Signup successful! Redirecting to login...");
        setIsError(false);

        setTimeout(() => {
          navigate("/login");
        }, 2000);
      } else {
        setMessage(data.message || "Signup failed - Username already exists");
        setIsError(true);
      }
    } catch (err) {
      console.error(err);
      setMessage(err.response?.data?.message || "Signup failed");
      setIsError(true);
    }
  };

  return (
    <div style={{ maxWidth: "400px", margin: "50px auto" }}>
      <h2>Signup</h2>

      <form onSubmit={handleSubmit}>
        <div>
          <label>Username:</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </div>

        <div style={{ marginTop: "10px" }}>
          <label>Password:</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>

        <button style={{ marginTop: "20px" }} type="submit">
          Signup
        </button>

        {message && (
          <p style={{ color: isError ? "red" : "green", marginTop: "15px" }}>
            {message}
          </p>
        )}
      </form>
    </div>
  );
}
