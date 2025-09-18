import { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";

export default function Collections() {
  const location = useLocation();
  const { dbId, dbName } = location.state || {};

  const [collections, setCollections] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  const token = localStorage.getItem("authToken");

  // 🔹 Fetch collections
  useEffect(() => {
    const fetchCollections = async () => {
      setError("");
      setLoading(true);

      if (!token) {
        setError("You are not logged in");
        setLoading(false);
        return;
      }

      try {
        const response = await fetch("http://localhost:9015/api/load-collections", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({ databaseName: dbName }),
        });

        if (!response.ok) {
          const errData = await response.json();
          setError(errData.message || "Failed to load collections");
        } else {
          const data = await response.json();
          setCollections(data);
        }
      } catch (err) {
        setError("Server error: " + err.message);
      } finally {
        setLoading(false);
      }
    };

    if (dbName) fetchCollections();
  }, [dbName, token]);

  // 🔹 Delete collection
  const handleDelete = async (collectionName) => {
    if (!window.confirm(`Are you sure you want to delete collection "${collectionName}"?`)) return;

    try {
      const response = await fetch(
        `http://localhost:9015/api/delete-collection?dbName=${dbName}`,
        {
          method: "DELETE",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({ collectionName }),
        }
      );

      if (!response.ok) {
        const errData = await response.json();
        alert(errData.message || "Failed to delete collection");
      } else {
        setCollections((prev) =>
          prev.filter((c) => c.collectionName !== collectionName)
        );
      }
    } catch (err) {
      alert("Server error: " + err.message);
    }
  };

  // 🔹 Rename collection
  const handleRename = async (oldName) => {
    const newName = prompt("Enter new collection name:", oldName);
    if (!newName || newName === oldName) return;

    try {
      const response = await fetch(
        `http://localhost:9015/api/rename-collection?dbName=${dbName}`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({
            oldDirectoryName: oldName,
            newDirectoryName: newName,
          }),
        }
      );

      if (!response.ok) {
        const errData = await response.json();
        alert(errData.message || "Failed to rename collection");
      } else {
        setCollections((prev) =>
          prev.map((c) =>
            c.collectionName === oldName ? { ...c, collectionName: newName } : c
          )
        );
      }
    } catch (err) {
      alert("Server error: " + err.message);
    }
  };

  if (loading) return <p>Loading collections...</p>;

  return (
    <div style={{ padding: "20px" }}>
      <h2>Collections in Database: {dbName}</h2>
      {error && <p style={{ color: "red" }}>{error}</p>}

      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fill, minmax(200px, 1fr))",
          gap: "20px",
          marginTop: "20px",
          maxWidth: "1200px",
        }}
      >
        {collections.map((col) => (
          <div
            key={col.collectionId}
            style={{
              border: "1px solid #ccc",
              borderRadius: "10px",
              padding: "15px",
              textAlign: "center",
              minHeight: "150px",
              display: "flex",
              flexDirection: "column",
              justifyContent: "space-between",
              backgroundColor: "#f8f8f8",
              boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
              transition: "0.2s",
            }}
          >
            <div>
              <h3 style={{ margin: "0 0 10px 0" }}>{col.collectionName}</h3>
              <p style={{ margin: 0 }}>ID: {col.collectionId}</p>
            </div>
            <div
              style={{
                display: "flex",
                justifyContent: "center",
                gap: "10px",
                marginTop: "10px",
              }}
            >
              <button
                style={{ padding: "4px 8px" }}
                onClick={() => handleRename(col.collectionName)}
              >
                Rename
              </button>
              <button
                style={{ padding: "4px 8px", backgroundColor: "red", color: "#fff" }}
                onClick={() => handleDelete(col.collectionName)}
              >
                Delete
              </button>
            </div>
          </div>
        ))}

        {/* Create new collection card */}
        <div
          style={{
            border: "1px dashed #888",
            borderRadius: "10px",
            padding: "30px",
            textAlign: "center",
            cursor: "pointer",
            minHeight: "150px",
            display: "flex",
            flexDirection: "column",
            justifyContent: "center",
            alignItems: "center",
            backgroundColor: "#f0f0f0",
            boxShadow: "0 2px 8px rgba(0,0,0,0.05)",
            transition: "0.2s",
            fontSize: "24px",
            fontWeight: "bold",
          }}
        >
          <div style={{ fontSize: "40px", marginBottom: "10px" }}>+</div>
          <div style={{ fontSize: "16px", fontWeight: "normal" }}>
            Create New Collection
          </div>
        </div>
      </div>
    </div>
  );
}
