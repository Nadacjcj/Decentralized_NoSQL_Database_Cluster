import { useState } from "react";

export default function FindQuery({ setResults, dbName, collectionName }) {
  const [ands, setAnds] = useState([{ field: "", operator: "eq", value: "" }]);
  const [ors, setOrs] = useState([{ field: "", operator: "eq", value: "" }]);

  const operators = ["eq", "ne", "gt", "gte", "lt", "lte"];
  const token = localStorage.getItem("authToken");

  const handleAndChange = (index, key, value) => {
    const newAnds = [...ands];
    newAnds[index][key] = value;
    setAnds(newAnds);
  };

  const handleOrChange = (index, key, value) => {
    const newOrs = [...ors];
    newOrs[index][key] = value;
    setOrs(newOrs);
  };

  const addAnd = () => setAnds([...ands, { field: "", operator: "eq", value: "" }]);
  const addOr = () => setOrs([...ors, { field: "", operator: "eq", value: "" }]);
  const removeAnd = (index) => setAnds(ands.filter((_, i) => i !== index));
  const removeOr = (index) => setOrs(ors.filter((_, i) => i !== index));

  const executeFind = async () => {
    const body = { ands, ors };

    try {
      const response = await fetch(
        `http://localhost:9015/api/findQuery?databaseName=${dbName}&collectionName=${collectionName}`,
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
        alert(err.message || "Find query failed");
      } else {
        const data = await response.json();
        setResults(Array.isArray(data) ? data : [data]);
      }
    } catch (err) {
      alert("Server error: " + err.message);
    }
  };

  return (
    <div>
      <h3>AND Conditions</h3>
      {ands.map((and, i) => (
        <div key={i} style={{ border: "1px solid #4caf50", padding: "10px", borderRadius: "5px", marginBottom: "8px" }}>
          <input placeholder="Field" value={and.field} onChange={(e) => handleAndChange(i, "field", e.target.value)} style={{ marginRight: "10px" }} />
          <select value={and.operator} onChange={(e) => handleAndChange(i, "operator", e.target.value)} style={{ marginRight: "10px" }}>
            {operators.map((op) => <option key={op} value={op}>{op}</option>)}
          </select>
          <input placeholder="Value" value={and.value} onChange={(e) => handleAndChange(i, "value", e.target.value)} style={{ marginRight: "10px" }} />
          <button onClick={() => removeAnd(i)}>Delete</button>
        </div>
      ))}
      <button onClick={addAnd} style={{ marginTop: "10px" }}>Add AND</button>

      <h3>OR Conditions</h3>
      {ors.map((or, i) => (
        <div key={i} style={{ border: "1px solid #2196f3", padding: "10px", borderRadius: "5px", marginBottom: "8px" }}>
          <input placeholder="Field" value={or.field} onChange={(e) => handleOrChange(i, "field", e.target.value)} style={{ marginRight: "10px" }} />
          <select value={or.operator} onChange={(e) => handleOrChange(i, "operator", e.target.value)} style={{ marginRight: "10px" }}>
            {operators.map((op) => <option key={op} value={op}>{op}</option>)}
          </select>
          <input placeholder="Value" value={or.value} onChange={(e) => handleOrChange(i, "value", e.target.value)} style={{ marginRight: "10px" }} />
          <button onClick={() => removeOr(i)}>Delete</button>
        </div>
      ))}
      <button onClick={addOr} style={{ marginTop: "10px" }}>Add OR</button>

      <button onClick={executeFind} style={{ marginTop: "20px", backgroundColor: "#4caf50", color: "#fff", padding: "8px 12px", borderRadius: "5px" }}>
        Execute Find
      </button>
    </div>
  );
}
