# Multicast chat
>### Como utilizar
> - Se direcione até o diretorio que contem os arquivos .class:
>```bash
>cd out/production/chat-multicast-java-ArthurRAmaral/
>```
>- Para executar o server:
>```bash
>java MultCastMessageServer
>```
>- Para executar o client:
>```bash
>java MultCastMessageClient
>```
 
---
>### Ao iniciar
>Ao iniciar o Server, ele irá printar no console todas as mensagens que ele receber.
>Mesmo que tais mensagens não sejam as que o usuário digitou, mas elas correspondem ao que o Client enviou.
>
>Já o Client inicia requisitando o seu nome de usuário.
>Após isso, insira um dos comandos diponíveis [Comandos disponíveis](#commands) para se comunicar com o Server.
---
> ### <a name="commands"></a> Comandos disponíveis
> ##### Quando fora de uma sala 
> - ###### `/end` - Para finalizar o Client
> - ###### `/newroom <nome-da-sala> <multicast-address>` - Para se criar uma nova sala e entrar nela
> - ###### `/allrooms` - Para listar todas as salas existentes
> - ###### `/join <nome-da-sala>` - Para entrar em uma sala já existente
> ##### Quando dentro de uma sala 
> - ###### `/members` - Para listar todos os membros de uma sala
> - ###### `/leave` - Para sair de uma sala
