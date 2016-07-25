import React from 'react'
import { render } from 'react-dom'
import ReactDOM from 'react-dom';
import { Link } from 'react-router'
import configureStore from '../store/configureStore';
import TodoAppRoot from '../containers/TodoAppRoot';
import Graph from '../components/graph/Graph'
import joint from 'jointjs'
import 'jointjs/dist/joint.css'
import _ from 'lodash'

const store = configureStore();

export const App = React.createClass({
    render: function() {
        return (
            <div>
                <aside>
                    <ul className="nav nav-pills nav-stacked">
                        <li><Link to={Process.path}>{Process.header}</Link></li>
                        <li><Link to={Visualization.path}>{Visualization.header}</Link></li>
                        <li><Link to={TodoApp.path}>{TodoApp.header}</Link></li>
                    </ul>
                </aside>
                <main>
                    {this.props.children}
                </main>
            </div>
        )
    }
});
App.title = 'Home'
App.path = '/'

export const Process = () => (
    <div className="Page">
        <h1>{Process.header}</h1>
        <p>Zakladka z procesami</p>
    </div>
);

Process.title = 'Process'
Process.path = '/process'
Process.header = 'Procesy'

export const Visualization = () => {
    var graphData = {
//TODO
        "edges": [{ "idIn": "abc", "idOut": "55", "label": ""}, { "idIn": "55", "idOut": "56", "label": "yes"}, { "idIn": "53", "idOut": "54", "label": ""}, { "idIn": "52", "idOut": "53", "label": ""}, { "idIn": "51", "idOut": "52", "label": "yes"}, { "idIn": "56", "idOut": "51", "label": "a=>a.<=(5)"}, { "idIn": "49", "idOut": "50", "label": ""}, { "idIn": "56", "idOut": "49", "label": "a=>a.>(5)"}]
    }
    return (
        <div className="Page">
            <h1>{Visualization.header}</h1>
            <p>Tutaj bedzie wizualizacja procesu</p>
            <Graph data={graphData}/>
        </div>
    )
}

Visualization.title = 'Visualization'
Visualization.path = '/visualization'
Visualization.header = 'Wizualizacja'

export const TodoApp = () => (

    <div className="Page">
        <TodoAppRoot store={ store }/>
    </div>
)

TodoApp.title = 'TodoApp'
TodoApp.path = '/todoApp'
TodoApp.header = 'TodoApp'