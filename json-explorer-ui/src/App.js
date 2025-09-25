import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Signup from "./pages/SignupTemp";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import Collections from "./pages/Collections";
import Documents from "./pages/Documents";

import FindQuery from "./pages/FindQuery";
import InsertQuery from "./pages/InsertQuery";
import UpdateQuery from "./pages/UpdateQuery";
import DeleteQuery from "./pages/DeleteQuery";

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/signup" element={<Signup />} />
        <Route path="/login" element={<Login />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/collections" element={<Collections />} />
        <Route path="/documents" element={<Documents />} />
      </Routes>
    </Router>
  );
}

export default App;
