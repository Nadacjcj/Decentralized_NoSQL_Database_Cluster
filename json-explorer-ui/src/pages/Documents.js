import { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import FindQuery from "./FindQuery";
import InsertQuery from "./InsertQuery";
import UpdateQuery from "./UpdateQuery";
import DeleteQuery from "./DeleteQuery";

export default function Documents() {
  const location = useLocation();
  const { dbName, collectionName } = location.state || {};

  const [queryType, setQueryType] = useState("find");
  const [results, setResults] = useState([]);
  const [schema, setSchema] = useState(null);

  const token = localStorage.getItem("authToken");

  useEffect(() => {
    // Fetch schema on mount or when db/collection changes
    if (dbName && collectionName) {
      fetch(`http://localhost:9015/api/get-schema?databaseName=${dbName}&collectionName=${collectionName}`, {
        headers: { Authorization: `Bearer ${token}` },
      })
        .then((res) => res.json())
        .then((data) => setSchema(data))
        .catch((err) => console.error("Failed to load schema", err));
    }
  }, [dbName, collectionName, token]);

  const renderQueryPage = () => {
    switch (queryType) {
      case "insert":
        return <InsertQuery setResults={setResults} dbName={dbName} collectionName={collectionName} onInsert={() => {}} />;
      case "update":
        return <UpdateQuery setResults={setResults} dbName={dbName} collectionName={collectionName} />;
      case "delete":
        return <DeleteQuery setResults={setResults} dbName={dbName} collectionName={collectionName} />;
      default:
        return <FindQuery setResults={setResults} dbName={dbName} collectionName={collectionName} />;
    }
  };

  return (
    <div style={{ display: "flex", padding: "20px", gap: "20px" }}>
      {/* Left panel: query builder */}
      <div style={{ flex: 1, maxWidth: "600px" }}>
        <h2>Documents in Collection: {collectionName}</h2>
        <p>Database: {dbName}</p>

       {schema && (
  <div
    style={{
      border: "1px solid #ccc",
      padding: "15px",
      marginBottom: "20px",
      borderRadius: "12px",
      background: "linear-gradient(120deg, #f0f8ff, #e0f7fa)",
      boxShadow: "0 4px 12px rgba(0,0,0,0.1)",
      fontFamily: "'Fira Code', monospace",
      fontSize: "14px",
      maxHeight: "300px",
      overflowY: "auto",
    }}
  >
    <h4 style={{ marginBottom: "10px", color: "#00796b" }}>Schema</h4>
    {Object.entries(schema).map(([key, value]) => (
      <div
        key={key}
        style={{
          marginBottom: "6px",
          padding: "4px 8px",
          borderRadius: "6px",
          backgroundColor: "#ffffffaa",
          display: "inline-block",
          wordBreak: "break-word",
        }}
      >
        <span style={{ color: "#2196f3" }}>"{key}"</span>: {JSON.stringify(value)}
      </div>
    ))}
  </div>
)}


        <select
          value={queryType}
          onChange={(e) => setQueryType(e.target.value)}
          style={{ marginBottom: "20px", padding: "8px", width: "200px" }}
        >
          <option value="find">Find Document</option>
          <option value="insert">Insert Document</option>
          <option value="update">Update Document</option>
          <option value="delete">Delete Document</option>
        </select>

        {renderQueryPage()}
      </div>

      {/* Right panel: results */}
      <div style={{ flex: 1, borderLeft: "1px solid #ccc", paddingLeft: "20px", maxHeight: "80vh", overflowY: "auto" }}>
        <h3>Query Results</h3>
        {results.length > 0 ? (
          <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
            {results.map((doc, index) => (
              <div
                key={index}
                style={{
                  border: "2px solid #4caf50",
                  borderRadius: "8px",
                  padding: "12px",
                  backgroundColor: "#f0f8ff",
                  fontWeight: "bold",
                  color: "#333",
                }}
              >
                {Object.entries(doc).map(([key, value]) => (
                  <div key={key} style={{ marginBottom: "4px" }}>
                    <span style={{ color: "#2196f3" }}>{key}:</span> <span>{JSON.stringify(value)}</span>
                  </div>
                ))}
              </div>
            ))}
          </div>
        ) : (
          <p>Results will appear here.</p>
        )}
      </div>
    </div>
  );
}
