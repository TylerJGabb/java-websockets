import React from 'react';

function Runner(props){
    const style = {
        background: props.tr.runId ? "yellow" : "green",
    }
    const running = props.tr.runId ? " RUN: " + props.tr.runId : ""
    return (
        <div className="Runner" style={style}>
            {`${props.tr.name}: ${props.tr.status}${running}`}
        </div>
    )
}

export default Runner;