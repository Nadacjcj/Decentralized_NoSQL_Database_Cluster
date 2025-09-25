import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import AceEditor from "react-ace";

import "ace-builds/src-noconflict/mode-json";
import "ace-builds/src-noconflict/theme-github";

export default function Collections() {
  const location = useLocation();
  const navigate = useNavigate();
  const { dbId, dbName } = location.state || {};

  const [collections, setCollections] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  // Create collection modal states
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newCollectionName, setNewCollectionName] = useState("");
  const [newSchema, setNewSchema] = useState(`{
  "cutie": {
    "required": false,
    "type": "boolean",
    "index": true
  },
  "name": {
    "required": true,
    "type": "string",
    "index": false
  }
}`);
  const [schemaError, setSchemaError] = useState("");

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
        `http://localhost:9015/api/delete-collection/${dbName}`,
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
        `http://localhost:9015/api/rename-collection/${dbName}`,
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

  // 🔹 Create collection
  const handleCreateCollection = async () => {
    setSchemaError("");

    if (!newCollectionName.trim()) {
      setSchemaError("Collection name cannot be empty");
      return;
    }

    let parsedSchema;
    try {
      parsedSchema = JSON.parse(newSchema);
    } catch (err) {
      setSchemaError("Invalid JSON schema: " + err.message);
      return;
    }

    try {
      const response = await fetch(
        `http://localhost:9015/api/create-collection/${dbName}`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({
            collectionName: newCollectionName,
            schema: parsedSchema,
          }),
        }
      );

      if (!response.ok) {
        const errData = await response.json();
        setSchemaError(errData.message || "Failed to create collection");
      } else {
        setShowCreateModal(false);
        setNewCollectionName("");
        setNewSchema(`{
  "cutie": {
    "required": false,
    "type": "boolean",
    "index": true
  },
  "name": {
    "required": true,
    "type": "string",
    "index": false
  }
}`);
        const created = await response.json();
        setCollections((prev) => [...prev, created]);
      }
    } catch (err) {
      setSchemaError("Server error: " + err.message);
    }
  };

  if (loading) return <p>Loading collections...</p>;

  return (
    <div style={{ padding: "20px" }}>
      <h2>Collections in Database: {dbName}</h2>
      {error && <p style={{ color: "red" }}>{error}</p>}

      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(200px, 1fr))", gap: "20px", marginTop: "20px", maxWidth: "1200px" }}>
        {collections.map((col) => (
          <div
            key={col.collectionId}
            onClick={() => navigate("/documents", { state: { dbId, dbName, collectionId: col.collectionId, collectionName: col.collectionName } })}
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
              cursor: "pointer"
            }}
          >
            <div>
              <h3 style={{ margin: "0 0 10px 0" }}>{col.collectionName}</h3>
              <p style={{ margin: 0 }}>ID: {col.collectionId}</p>
            </div>
            <div style={{ display: "flex", justifyContent: "center", gap: "10px", marginTop: "10px" }}>
              <button style={{ padding: "4px 8px" }} onClick={(e) => { e.stopPropagation(); handleRename(col.collectionName); }}>Rename</button>
              <button style={{ padding: "4px 8px", backgroundColor: "red", color: "#fff" }} onClick={(e) => { e.stopPropagation(); handleDelete(col.collectionName); }}>Delete</button>
            </div>
          </div>
        ))}

        {/* Create new collection card */}
        <div onClick={() => setShowCreateModal(true)} style={{ border: "1px dashed #888", borderRadius: "10px", padding: "30px", textAlign: "center", cursor: "pointer", minHeight: "150px", display: "flex", flexDirection: "column", justifyContent: "center", alignItems: "center", backgroundColor: "#f0f0f0", boxShadow: "0 2px 8px rgba(0,0,0,0.05)", transition: "0.2s", fontSize: "24px", fontWeight: "bold" }}>
          <div style={{ fontSize: "40px", marginBottom: "10px" }}>+</div>
          <div style={{ fontSize: "16px", fontWeight: "normal" }}>Create New Collection</div>
        </div>
      </div>

      {/* Create Collection Modal */}
      {showCreateModal && (
        <div style={{ position: "fixed", top: 0, left: 0, right: 0, bottom: 0, backgroundColor: "rgba(0,0,0,0.5)", display: "flex", justifyContent: "center", alignItems: "center", zIndex: 9999 }}>
          <div style={{ backgroundColor: "#fff", padding: "30px", borderRadius: "10px", width: "600px", maxHeight: "90vh", overflowY: "auto" }}>
            <h3>Create New Collection</h3>
            <input
              type="text"
              placeholder="Collection Name"
              value={newCollectionName}
              onChange={(e) => setNewCollectionName(e.target.value)}
              style={{ width: "100%", padding: "8px", marginBottom: "15px" }}
            />

            <label style={{ fontWeight: "bold" }}>Schema (JSON)</label>
            <AceEditor
              mode="json"
              theme="github"
              value={newSchema}
              onChange={setNewSchema}
              name="schemaEditor"
              editorProps={{ $blockScrolling: true }}
              width="100%"
              height="300px"
              setOptions={{
                useWorker: false,
                showLineNumbers: true,
                tabSize: 2,
              }}
            />
            <p style={{ color: "red", fontSize: "12px", marginTop: "5px" }}>Schema is required and cannot contain invalid JSON.</p>
            {schemaError && <p style={{ color: "red" }}>{schemaError}</p>}

            <div style={{ display: "flex", justifyContent: "flex-end", gap: "10px", marginTop: "15px" }}>
              <button onClick={() => setShowCreateModal(false)}>Cancel</button>
              <button style={{ backgroundColor: "blue", color: "#fff", padding: "6px 16px" }} onClick={handleCreateCollection}>Create</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
