import {css, html, LitElement} from 'lit';
import {JsonRpc} from 'jsonrpc';
import {notifier} from 'notifier';
import '@vaadin/button';
import '@vaadin/icon';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset';

export class QwcMinecraftRespawn extends LitElement {

    jsonRpc = new JsonRpc(this);

    static styles = css`
        :host {
            display: flex;
            flex-direction: column;
            gap: 10px;
            height: 100%;
            padding-left: 10px;
            padding-right: 10px;
        }

    `;

    constructor() {
        super();
    }

    render() {
        return html`
            <vaadin-button theme="primary" @click="${this._setRespawn}">
                <vaadin-icon icon="vaadin:flag" slot="prefix"></vaadin-icon>
                Set A New Spawn Point
            </vaadin-button>
            <vaadin-button theme="primary" @click="${this._killAndRespawnPlayer}">
                <vaadin-icon icon="vaadin:play" slot="prefix"></vaadin-icon>
                Respawn
            </vaadin-button>
        `;
    }

    _setRespawn() {
        notifier.showInfoMessage('Setting respawn point...');

        this.jsonRpc.setRespawn().then(() => {
            notifier.showSuccessMessage('Respawn point set');
        }).catch(error => {
            notifier.showErrorMessage(error.message || 'Failed to set a new respawn point');
        });
    }

    _killAndRespawnPlayer() {
        notifier.showInfoMessage('Killing player...');
        return this.jsonRpc.killPlayer().then(() => {
            notifier.showInfoMessage('Player killed — respawning...');
            return this.jsonRpc.respawnPlayer();
        }).then(() => {
            notifier.showSuccessMessage('Player respawned at new location');
        }).catch(error => {
            notifier.showErrorMessage(error.message || 'Failed to respawn');
        });
    }
}

customElements.define('qwc-minecraft-respawn', QwcMinecraftRespawn);

// Made with Bob
