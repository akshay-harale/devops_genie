import React from 'react';
import "./Header.css"

const Header = () => {
    return(
        <header>
        <nav className='navigation'>
        <div className="brand">DEVOPS GENIE</div>
          <ul className="nav-links">

            <li>Home</li>
            <li>About</li>
            <li>Contact</li>
          </ul>
        </nav>
      </header>
    )
}

export default Header;