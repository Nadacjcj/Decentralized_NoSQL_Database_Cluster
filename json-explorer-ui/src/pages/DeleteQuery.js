import { useState } from "react";

export default function DeleteQuery({ setResults, dbName, collectionName }) {
  const [ands, setAnds] = useState([{ field: "", operator: "eq", value: "" }]);
  const [ors, setOrs] = useState([{ field: "", operator: "eq", value: "" }]);
  const operators = ["eq", "ne", "gt", "gte", "lt", "lte"];
  const token = localStorage.getItem("authToken");

  // AND handlers
  const handleAndChange = (index, key, value) => {
    const newAnds = [...ands];
    newAnds[index][key] = value;
    setAnds(newAnds);
  };
  const addAnd = () => setAnds([...ands, { field: "", operator: "eq", value: "" }]);
  const removeAnd = (index) => setAnds(ands.filter((_, i) => i !== index));

  // OR handlers
  const handleOrChange = (index, key, value) => {
    const newOrs = [...ors];
    newOrs[index][key] = value;
    setOrs(newOrs);
  };
  const addOr = () => setOrs([...ors, { field: "", operator: "eq", value: "" }]);
  const removeOr = (index) => setOrs(ors.filter((_, i) => i !== index));

  // Submit delete
  const submitDelete = async () => {
    const body = { ands, ors };
    try {
      const response = await fetch(
        `http://localhost:9015/api/deleteQuery?databaseName=${dbName}&collectionName=${collectionName}`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify(body),
        }
      );

      if (!response.ok) {
        const err = await response.json();
        alert(err.message || "Delete query failed");
      } else {
        const data = await response.text();
        alert("Delete successful!");
        if (setResults) setResults(prev => prev); // Optionally refresh results
      }
    } catch (err) {
      alert("Server error: " + err.message);
    }
  };

  return (
    <div style={{ border: "1px solid #ccc", padding: "10px", borderRadius: "5px" }}>
      <h4>AND Conditions</h4>
      {ands.map((and, i) => (
        <div key={i} style={{ border: "1px solid #4caf50", padding: "8px", borderRadius: "5px", marginBottom: "6px" }}>
          <input placeholder="Field" value={and.field} onChange={(e) => handleAndChange(i, "field", e.target.value)} style={{ marginRight: "8px" }} />
          <select value={and.operator} onChange={(e) => handleAndChange(i, "operator", e.target.value)} style={{ marginRight: "8px" }}>
            {operators.map(op => <option key={op} value={op}>{op}</option>)}
          </select>
          <input placeholder="Value" value={and.value} onChange={(e) => handleAndChange(i, "value", e.target.value)} style={{ marginRight: "8px" }} />
          <button onClick={() => removeAnd(i)}>Delete</button>
        </div>
      ))}
      <button onClick={addAnd} style={{ marginBottom: "12px" }}>Add AND</button>

      <h4>OR Conditions</h4>
      {ors.map((or, i) => (
        <div key={i} style={{ border: "1px solid #2196f3", padding: "8px", borderRadius: "5px", marginBottom: "6px" }}>
          <input placeholder="Field" value={or.field} onChange={(e) => handleOrChange(i, "field", e.target.value)} style={{ marginRight: "8px" }} />
          <select value={or.operator} onChange={(e) => handleOrChange(i, "operator", e.target.value)} style={{ marginRight: "8px" }}>
            {operators.map(op => <option key={op} value={op}>{op}</option>)}
          </select>
          <input placeholder="Value" value={or.value} onChange={(e) => handleOrChange(i, "value", e.target.value)} style={{ marginRight: "8px" }} />
          <button onClick={() => removeOr(i)}>Delete</button>
        </div>
      ))}
      <button onClick={addOr} style={{ marginBottom: "12px" }}>Add OR</button>

      <button onClick={submitDelete} style={{ backgroundColor: "#f44336", color: "#fff", padding: "8px 12px", borderRadius: "5px" }}>
        Submit Delete
      </button>
    </div>
  );
}
