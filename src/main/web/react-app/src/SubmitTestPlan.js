import React from 'react';
import {
  Redirect
} from 'react-router-dom';

//https://thestyleofelements.org/the-art-of-the-error-message-9f878d0bff80
export default class SubmitTestPlan extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      finished: false
    }
    this.handleSubmit = this.handleSubmit.bind(this);
  }


  render() {
    //TODO: Handle state inside react component, not inside input elements
    if(this.state.finished) return <Redirect to='/' />;
    return (
      <form onSubmit={this.handleSubmit}>
        <p>Some fields are limited to prevent over-allocation of resources</p>
        <label>Priority
          <input type="number" name="priority" min="1" max="10" required={true}/>
          <br />
        </label>
        <label>Max TestRunners
          <input type="number" name="maxTestRunners" min="1" required={true}/>
          <br />
        </label>
        <label>Max Allowed Failures
          <input type="number" name="maximumAllowedFailures" min="0" max="10" required={true}/>
          <br />
        </label>
        <label>Required Passes
          <input type="number" name="requiredPasses" min="0" max="10" required={true}/>
          <br />
        </label>
        <input type="submit" />
      </form>
    )
  }

  handleSubmit(event) {
    event.preventDefault();
    let formData = new FormData(event.target);
    console.log(formData);
    let body = {};
    for(let [key, value] of formData.entries()) {
      console.log(key, value);
      body[key] = value;
    }
    fetch('http://localhost:8082/api/testPlans', { 
      method: 'POST',
      body: JSON.stringify(body),
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      }
    })
    .then(resp => resp.text())
    .then(text => {
      console.log("CREATED TP " + text);
      this.setState({finished : true});
    })
    .catch(console.error);
  }
}