import React from 'react';
import MinimalRunnerDashboard from './MinimalRunnerDashboard';
import SubmitTestPlan from './SubmitTestPlan';
//https://reacttraining.com/react-router/web/example/basic
import {
  BrowserRouter as Router,
  Switch,
  Route,
  Link
} from "react-router-dom";
class App extends React.Component {

  render() {
    return (
      <Router>
        <div>
          <ul>
            <li><Link to="/">Home</Link></li>
            <li><Link to="/runnerdash">RunnerDashboard</Link></li>
            <li><Link to="/submitTestPlan">Submit Test Plan</Link></li>
            <li><Link to="/testPlanStatus">Test Plan Status</Link></li>
          </ul>
          <hr />
          <Switch>
            <Route exact path="/">
              <h1>Test Runner Manager 2.0</h1>
              <p>
                Use the links provided in the header.
              </p>
              <h2>Updates:</h2>
              <p>
                Some text goes here that explains recent updates, issues, fixes, workarounds
              </p>
            </Route>
            <Route exact path="/runnerDash">
              <MinimalRunnerDashboard />
            </Route>
            <Route exact path="/submitTestPlan">
              <SubmitTestPlan />
            </Route>
            <Route exact path="/testPlanStatus">
              TODO: Show active and completed plans, with percent complete, allow sorting/searching
            </Route>
          </Switch>
          <hr />
        </div>
      </Router>
    )
  }
}

export default App;