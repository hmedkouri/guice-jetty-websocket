environments {
    test {
        appId = 'chat-app'
        env = 'test'
        key = System.getenv('acuo_security_key')
    }

    integ {
        appId = 'chat-app'
        env = 'int'
        key = System.getenv('acuo_security_key')
    }
    
    docker {
        appId = 'chat-app'
        env = 'DOCKER_ENV_TOKEN'
        key = System.getenv('acuo_security_key')
    }
}