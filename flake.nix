{
  description = "Nix flake for Digisaur development";

  inputs.nixpkgs.url = "github:nixos/nixpkgs?ref=nixos-unstable";

  outputs = { self, nixpkgs}:
    let
      system = "x86_64-linux";
      pkgs = import nixpkgs { inherit system; };
      jdk = pkgs.jdk21;
    in {
      devShells.x86_64-linux.default = pkgs.mkShell {
        packages = with pkgs; [
			gradle
        ];
        shellHook = ''
          exec fish
        '';
      };
    };
}
