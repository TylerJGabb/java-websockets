import React from 'react';

class MinimalRunner extends React.Component{

    constructor(props){
        super(props);
        this.toggle = this.toggle.bind(this);
    }

    toggle(){
        let disconnected = this.props.tr.status === 'WEBSOCKET_DISCONNECTED';
        let url = `http://${this.props.tr.host}:${this.props.tr.portalPort}/api/config/${disconnected ? 'up' : 'down'}`;
        fetch(url, {method: 'PUT' })
        .then(console.log)
        .catch(console.error);
    }


    render(){
        let background = '#7FFF00';
        let disconnected = this.props.tr.status === 'WEBSOCKET_DISCONNECTED';
        if(disconnected){
            background = 'red'
        } else if (this.props.tr.runId){
            background = 'yellow'
        }
        const running = this.props.tr.runId ? " RUN: " + this.props.tr.runId : ""
        return (
            <div className="Runner" style={{background, borderStyle: 'solid'}}>
                <p>{`Host: ${this.props.tr.host} Status: ${this.props.tr.status}${running}`}</p>
                <button onClick={this.upDown}>{disconnected ? 'Enable' : 'Disable'}</button>
                <button>Configure</button>
            </div>
        )
    }

}

export default MinimalRunner;