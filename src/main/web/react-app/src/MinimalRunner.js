import React from 'react';

function MinimalRunner(props){
    let background = '#7FFF00';
    if(props.tr.status === 'WEBSOCKET_DISCONNECTED'){
        background = 'red'
    } else if (props.tr.runId){
        background = 'yellow'
    }
    const running = props.tr.runId ? " RUN: " + props.tr.runId : ""
    return (
        <div className="Runner" style={{background}}>
            {`Host: ${props.tr.host} Status: ${props.tr.status}${running}`}
        </div>
    )
}

export default MinimalRunner;