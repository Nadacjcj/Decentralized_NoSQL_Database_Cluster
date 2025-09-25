import { useState } from "react";

export default function InsertQuery({ dbName, collectionName, onInsert }) {
  const [jsonText, setJsonText] = useState("");
  const [error, setError] = useState(null);
  const [isOpen, setIsOpen] = useState(false);
  const token = localStorage.getItem("authToken");

  const openEditor = () => setIsOpen(true);
  const closeEditor = () => {
    setIsOpen(false);
    setJsonText("");
    setError(null);
  };

  const handleSubmit = async () => {
  try {
    const parsedJson = JSON.parse(jsonText); // convert text to JSON
    const response = await fetch(
      `http://localhost:9015/api/insertQuery?databaseName=${dbName}&collectionName=${collectionName}`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ document: parsedJson }),
      }
    );

    const responseBody = await response.text(); // get exact body

    if (!response.ok) {
      alert("Response :  " + responseBody); // show exact backend message
      setError(responseBody);
    } else {
      alert("Response : " + responseBody); // show exact backend message
      if (onInsert) onInsert(); // reload documents
      closeEditor();
    }
  } catch (e) {
    const msg = "Invalid JSON or server error: " + e.message;
    setError(msg);
    alert(msg);
  }
};


  return (
    <>
      <button
        onClick={openEditor}
        style={{
          backgroundColor: "#4caf50",
          color: "#fff",
          padding: "8px 12px",
          borderRadius: "5px",
          marginBottom: "10px",
          marginLeft: "20px",
        }}
      >
        Insert Document
      </button>

      {isOpen && (
        <div
          style={{
            position: "fixed",
            top: 0,
            left: 0,
            width: "100vw",
            height: "100vh",
            backgroundColor: "rgba(0,0,0,0.5)",
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            zIndex: 1000,
          }}
        >
          <div
            style={{
              backgroundColor: "#fff",
              padding: "20px",
              borderRadius: "8px",
              width: "600px",
              maxHeight: "80vh",
              overflowY: "auto",
            }}
          >
            <h3>Insert JSON Document</h3>

            <textarea
              style={{ width: "100%", height: "300px", fontFamily: "monospace", fontSize: "14px" }}
              value={jsonText}
              onChange={(e) => setJsonText(e.target.value)}
              placeholder='Enter your JSON document here, e.g. { "field": 123, "flag": true }'
            />

            {error && <p style={{ color: "red" }}>{error}</p>}

            <div style={{ marginTop: "10px", display: "flex", gap: "10px" }}>
              <button
                onClick={handleSubmit}
                style={{
                  backgroundColor: "#4caf50",
                  color: "#fff",
                  padding: "8px 12px",
                  borderRadius: "5px",
                }}
              >
                Submit
              </button>
              <button
                onClick={closeEditor}
                style={{
                  backgroundColor: "#f44336",
                  color: "#fff",
                  padding: "8px 12px",
                  borderRadius: "5px",
                }}
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
