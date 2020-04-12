import React from 'react';
import MinimalRunner from './MinimalRunner';
const URL = 'http://localhost:8082/api/testRunners';

class MinimalRunnerDashboard extends React.Component{

    constructor(props){
        super(props);
        this.state = {
            runners: [],
            failedToConnect: false
        }
        this.fetchRunners = this.fetchRunners.bind(this);
    }

    fetchRunners(){
        fetch(URL).then(resp => resp.text()).then(txt => JSON.parse(txt))
        .then(arr => this.setState({ //this call forces re-render
            runners: arr,
            failedToConnect: false
        })) 
        .catch(e => {
            console.error(e);
            this.setState({ //this call forces re-render
                runners: [],
                failedToConnect: true
            });
        });
    }

    componentDidMount(){
        this.fetchRunners();
        setInterval(this.fetchRunners, 1000);
    }

    componentWillUnmount(){
        clearInterval(this.fetchRunners);
    }

    render(){
        const buttonTitle = 'loads page showing more detailed runner statistics, this has yet to be implemented';
        return (
            <div className="RunnerDashboard" style={{borderStyle: 'dashed'}}>
                {this.state.failedToConnect ? "CAN NOT FETCH RUNNERS, CONNECTION FAILED" : "RUNNERS:"}
                {this.state.runners.map(tr => <MinimalRunner key={tr.name} tr={tr}/>)}
                <button className="moreDetails" title={buttonTitle}>More Details</button>
            </div>
        );
    }
}

export default MinimalRunnerDashboard;