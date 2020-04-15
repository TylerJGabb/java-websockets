import React from 'react';

class MinimalRunner extends React.Component{

    constructor(props){
        super(props);
        this.upDown = this.upDown.bind(this);
    }

    upDown(){
        let disconnected = this.props.tr.status === 'WEBSOCKET_DISCONNECTED';
        let url = `http://localhost:${this.props.tr.portalPort}/api/config/${disconnected ? 'up' : 'down'}`;
        fetch(url, {
            method: 'PUT'
        }).then(response => console.log(response))
        .catch(e => alert(e));
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
            <div className="Runner" style={{background}}>
                {`Host: ${this.props.tr.host} Status: ${this.props.tr.status}${running}`}
                <button onClick={this.upDown}>{disconnected ? 'Enable' : 'Disable'}</button>
            </div>
        )
    }

}

export default MinimalRunner;