import React from 'react';
import MinimalRunnerDashboard from './MinimalRunnerDashboard'

function Main(){
    return (
        <div className="Main">
            <header className="Main-header">
                TEST RUNNER MANAGER
                <MinimalRunnerDashboard />
                {/* <TestPlanDashboard /> */}
            </header>

        </div>
    )
}

export default Main;