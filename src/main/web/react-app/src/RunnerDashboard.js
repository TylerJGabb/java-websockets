import React from 'react';
import Runner from './Runner';
const URL = 'http://localhost:8082/api/testRunners';

class RunnerDashboard extends React.Component{

    constructor(props){
        super(props);
        this.state = {
            runners: []
        }
        this.fetchRunners = this.fetchRunners.bind(this);
    }

    fetchRunners(){
        fetch(URL)
        .then(resp => resp.text())
        .then(txt => JSON.parse(txt))
        .then(arr => this.setState({runners: arr})) //this call forces re-render
        .catch(e => {
            console.error(e);
            this.setState({runners: []});
        });
    }

    componentDidMount(){
        setInterval(this.fetchRunners, 1000);
    }

    render(){
        return (
            <div className="RunnerDashboard">
                RUNNERS:
                {this.state.runners.map(tr => <Runner tr={tr}/>)}
            </div>
        );
    }
}

export default RunnerDashboard;