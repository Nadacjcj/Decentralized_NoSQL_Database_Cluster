import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

export default function Dashboard() {
  const [databases, setDatabases] = useState([]);
  const [error, setError] = useState("");
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newDbName, setNewDbName] = useState("");
  const [newDbDescription, setNewDbDescription] = useState("");
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [dbToDelete, setDbToDelete] = useState(null);
  const [showRenameModal, setShowRenameModal] = useState(false);
  const [dbToRename, setDbToRename] = useState(null);
  const [renameDbName, setRenameDbName] = useState("");

  const navigate = useNavigate();
  const token = localStorage.getItem("authToken");

  useEffect(() => {
    fetchDatabases();
  }, []);

  const fetchDatabases = async () => {
    setError("");
    if (!token) {
      setError("You are not logged in");
      return;
    }
    try {
      const response = await fetch("http://localhost:9015/api/load-databases", {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        const errData = await response.json();
        setError(errData.message || "Failed to load databases");
        return;
      }

      const data = await response.json();
      setDatabases(data);
    } catch (err) {
      setError("Server error: " + err.message);
    }
  };

const handleDatabaseClick = (db) => {
  navigate("/collections", { state: { dbId: db.id, dbName: db.name } });
};


  const handleCreateClick = () => setShowCreateModal(true);
  const handleCloseCreateModal = () => {
    setShowCreateModal(false);
    setNewDbName("");
    setNewDbDescription("");
  };

  const handleCreateDatabase = async () => {
    setError("");
    if (!token) return setError("You are not logged in");
    if (!newDbName) return setError("Database name cannot be empty");

    try {
      const response = await fetch("http://localhost:9015/api/create-database", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          databaseName: newDbName,
          description: newDbDescription,
        }),
      });

      if (response.ok) {
        handleCloseCreateModal();
        fetchDatabases();
      } else {
        const result = await response.json();
        setError(result || "Failed to create database");
      }
    } catch (err) {
      setError("Server error: " + err.message);
    }
  };

  // Delete functionality
  const handleDeleteClick = (db) => {
    setDbToDelete(db);
    setShowDeleteModal(true);
  };
  const handleCloseDeleteModal = () => {
    setDbToDelete(null);
    setShowDeleteModal(false);
  };
  const confirmDeleteDatabase = async () => {
    if (!token || !dbToDelete) return;

    try {
      const response = await fetch("http://localhost:9015/api/delete-database", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ databaseName: dbToDelete.name }),
      });

      if (response.ok) {
        handleCloseDeleteModal();
        fetchDatabases();
      } else {
        const result = await response.text();
        setError(result || "Failed to delete database");
      }
    } catch (err) {
      setError("Server error: " + err.message);
    }
  };

  // Rename functionality
  const handleRenameClick = (db) => {
    setDbToRename(db);
    setRenameDbName(db.name);
    setShowRenameModal(true);
  };
  const handleCloseRenameModal = () => {
    setDbToRename(null);
    setRenameDbName("");
    setShowRenameModal(false);
  };
  const confirmRenameDatabase = async () => {
    if (!token || !dbToRename || !renameDbName) return;

    try {
      const response = await fetch("http://localhost:9015/api/rename-database", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          oldDirectoryName: dbToRename.name,
          newDirectoryName: renameDbName,
        }),
      });

      if (response.ok) {
        handleCloseRenameModal();
        fetchDatabases();
      } else {
        const result = await response.text();
        setError(result || "Failed to rename database");
      }
    } catch (err) {
      setError("Server error: " + err.message);
    }
  };

  return (
    <div style={{ padding: "20px" }}>
      <h2>My Databases</h2>
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
        {databases.map((db) => (
          <div
            key={db.id}
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
            <div onClick={() => handleDatabaseClick(db)} style={{ cursor: "pointer" }}>
              <h3 style={{ margin: "0 0 10px 0" }}>{db.name}</h3>
              <p style={{ margin: 0 }}>{db.description}</p>
            </div>
            <div style={{ display: "flex", justifyContent: "center", gap: "10px", marginTop: "10px" }}>
              <button
                style={{ padding: "4px 8px" }}
                onClick={() => handleRenameClick(db)}
              >
                Rename
              </button>
              <button
                style={{ padding: "4px 8px", backgroundColor: "red", color: "#fff" }}
                onClick={() => handleDeleteClick(db)}
              >
                Delete
              </button>
            </div>
          </div>
        ))}

        {/* Create new database card */}
        <div
          onClick={handleCreateClick}
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
          <div style={{ fontSize: "16px", fontWeight: "normal" }}>Create New Database</div>
        </div>
      </div>

      {/* Create Modal */}
      {showCreateModal && (
        <div
          style={{
            position: "fixed",
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: "rgba(0,0,0,0.5)",
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            zIndex: 9999,
          }}
        >
          <div
            style={{
              backgroundColor: "#fff",
              padding: "30px",
              borderRadius: "10px",
              width: "400px",
              boxShadow: "0 4px 12px rgba(0,0,0,0.3)",
            }}
          >
            <h6>Create New Database</h6>
            <input
              type="text"
              placeholder="Database Name"
              value={newDbName}
              onChange={(e) => setNewDbName(e.target.value)}
              style={{ width: "100%", padding: "8px", marginBottom: "10px" }}
            />
            <input
              type="text"
              placeholder="Description"
              value={newDbDescription}
              onChange={(e) => setNewDbDescription(e.target.value)}
              style={{ width: "100%", padding: "8px", marginBottom: "10px" }}
            />
            <div style={{ display: "flex", justifyContent: "flex-end", gap: "10px" }}>
              <button onClick={handleCloseCreateModal}>Cancel</button>
              <button onClick={handleCreateDatabase}>Create</button>
            </div>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {showDeleteModal && (
        <div
          style={{
            position: "fixed",
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: "rgba(0,0,0,0.5)",
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            zIndex: 9999,
          }}
        >
          <div
            style={{
              backgroundColor: "#fff",
              padding: "30px",
              borderRadius: "10px",
              width: "350px",
              textAlign: "center",
              boxShadow: "0 4px 12px rgba(0,0,0,0.3)",
            }}
          >
            <h6>Are you sure you want to delete "{dbToDelete?.name}"?</h6>
            <div style={{ display: "flex", justifyContent: "center", gap: "20px", marginTop: "20px" }}>
              <button onClick={handleCloseDeleteModal}>Cancel</button>
              <button
                style={{ backgroundColor: "red", color: "#fff", padding: "6px 16px" }}
                onClick={confirmDeleteDatabase}
              >
                Yes
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Rename Modal */}
      {showRenameModal && (
        <div
          style={{
            position: "fixed",
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: "rgba(0,0,0,0.5)",
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            zIndex: 9999,
          }}
        >
          <div
            style={{
              backgroundColor: "#fff",
              padding: "30px",
              borderRadius: "10px",
              width: "350px",
              textAlign: "center",
              boxShadow: "0 4px 12px rgba(0,0,0,0.3)",
            }}
          >
            <h6>Rename Database "{dbToRename?.name}"</h6>
            <input
              type="text"
              placeholder="New Database Name"
              value={renameDbName}
              onChange={(e) => setRenameDbName(e.target.value)}
              style={{ width: "100%", padding: "8px", marginTop: "10px" }}
            />
            <div style={{ display: "flex", justifyContent: "center", gap: "20px", marginTop: "20px" }}>
              <button onClick={handleCloseRenameModal}>Cancel</button>
              <button
                style={{ backgroundColor: "blue", color: "#fff", padding: "6px 16px" }}
                onClick={confirmRenameDatabase}
              >
                Rename
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
